package com.necro.raid.dens.common.raids.adventure;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidBucket;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.RaidAdventureInstance;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.helpers.RaidRegionHelper;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import com.necro.raid.dens.common.registry.RaidRegistry;
import com.necro.raid.dens.common.util.ComponentUtils;
import com.necro.raid.dens.common.util.DoubleWeightedRandomMap;
import com.necro.raid.dens.common.util.IRaidAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RaidNode implements AdventureNode {
    public static final String ID = "raid";

    private final List<String> references;
    private transient @Nullable RaidInstance activeRaid;
    private transient boolean waitingForChoice;
    private transient boolean choiceMade;
    private transient @Nullable ResourceLocation option1;
    private transient @Nullable ResourceLocation option2;
    private transient @Nullable UUID choosingPlayerUuid;
    private boolean isFinished;

    public RaidNode(Collection<String> references) {
        this.references = new ArrayList<>(references);
        this.activeRaid = null;
        this.waitingForChoice = false;
        this.choiceMade = false;
        this.option1 = null;
        this.option2 = null;
        this.choosingPlayerUuid = null;
        this.isFinished = false;
    }

    @Override
    public int delay() {
        return 40;
    }

    public List<ResourceLocation> resolveBosses() {
        LinkedHashSet<ResourceLocation> bosses = new LinkedHashSet<>();
        for (String ref : this.references) {
            if (ref == null || ref.isEmpty()) continue;
            if (ref.startsWith("@")) {
                ResourceLocation bucketId = ResourceLocation.parse(ref.substring(1));
                RaidBucket bucket = CustomRaidRegistries.RAID_BUCKET_REGISTRY.get(bucketId);
                if (bucket != null) {
                    BitSet compiled = bucket.getCompiled();
                    for (int i = compiled.nextSetBit(0); i >= 0; i = compiled.nextSetBit(i + 1)) {
                        if (i < RaidRegistry.RAID_LIST.size()) bosses.add(RaidRegistry.RAID_LIST.get(i));
                    }
                }
            }
            else if (ref.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.parse(ref.substring(1));
                bosses.addAll(RaidRegistry.getTagEntries(tagId));
            }
            else {
                ResourceLocation bossId = ResourceLocation.parse(ref);
                if (RaidRegistry.exists(bossId)) bosses.add(bossId);
            }
        }
        return new ArrayList<>(bosses);
    }

    @Override
    public void run(RaidAdventureInstance adventure, MinecraftServer server) {
        List<ResourceLocation> resolved = this.resolveBosses();
        if (resolved.isEmpty()) {
            this.isFinished = true;
            return;
        }

        if (resolved.size() == 1) adventure.schedule(() -> this.startRaid(resolved.getFirst(), adventure, server), this.delay(), false);
        else {
            DoubleWeightedRandomMap<ResourceLocation> map = new DoubleWeightedRandomMap<>(false);
            for (ResourceLocation id : resolved) {
                Integer index = RaidRegistry.RAID_INDEX.get(id);
                if (index == null) continue;
                double weight = RaidRegistry.WEIGHTS_BY_INDEX[index];
                map.add(id, weight);
            }
            RandomSource random = server.overworld().getRandom();
            this.option1 = map.getRandom(random).orElseGet(resolved::getFirst);
            this.option2 = map.getRandom(random).orElseGet(() ->
                resolved.stream().filter(id -> !id.equals(this.option1)).findFirst().orElse(this.option1)
            );

            CompletableFuture<Optional<RaidBoss>> option1 = RaidRegistry.requestRaidBoss(this.option1);
            CompletableFuture<Optional<RaidBoss>> option2 = RaidRegistry.requestRaidBoss(this.option2);

            this.waitingForChoice = true;
            this.choiceMade = false;

            option1.thenAcceptBoth(option2, (boss1, boss2) -> adventure.schedule(() -> {
                ServerPlayer selectingPlayer = adventure.getSelectingPlayer(server);
                if (selectingPlayer == null) {
                    this.waitingForChoice = false;
                    this.choiceMade = true;
                    adventure.schedule(() -> this.startRaid(this.option1, adventure, server), this.delay(), false);
                    return;
                }

                this.choosingPlayerUuid = selectingPlayer.getUUID();
                RaidDenNetworkMessages.CHOOSE_RAID_BOSS.accept(selectingPlayer, adventure.getRaid(), this.option1, this.option2);
            }, this.delay(), false));

            adventure.schedule(() -> {
                if (this.waitingForChoice && !this.choiceMade) this.selectBoss(this.option1, adventure, server);
            }, 340, false);
        }
    }

    @Override
    public void onPlayerLeave(ServerPlayer player, RaidAdventureInstance adventure) {
        RaidDenNetworkMessages.REMOVE_BOSS_SELECTOR.accept(player, adventure.getRaid());
        if (player.getUUID().equals(this.choosingPlayerUuid) && this.waitingForChoice && !this.choiceMade) this.selectBoss(this.option1, adventure, player.getServer());
    }

    public void onBossSelected(ServerPlayer player, ResourceLocation selectedBoss, RaidAdventureInstance adventure, MinecraftServer server) {
        if (!this.waitingForChoice || this.choiceMade) return;
        if (this.choosingPlayerUuid != null && !this.choosingPlayerUuid.equals(player.getUUID())) return;
        if (selectedBoss == null || (!selectedBoss.equals(this.option1) && !selectedBoss.equals(this.option2))) selectedBoss = this.option1;
        this.selectBoss(selectedBoss, adventure, server);
    }

    public void selectBoss(ResourceLocation selected, RaidAdventureInstance adventure, MinecraftServer server) {
        ServerPlayer selectingPlayer = this.choosingPlayerUuid != null ? server.getPlayerList().getPlayer(this.choosingPlayerUuid) : null;
        if (selectingPlayer != null) RaidDenNetworkMessages.REMOVE_BOSS_SELECTOR.accept(selectingPlayer, adventure.getRaid());
        this.choiceMade = true;
        this.waitingForChoice = false;
        this.startRaid(selected, adventure, server);
    }

    public void startRaid(ResourceLocation bossId, RaidAdventureInstance adventure, MinecraftServer server) {
        RaidRegistry.requestRaidBoss(bossId, boss -> {
            Vec3 vec3;
            ServerLevel level;
            RaidRegion region = RaidRegionHelper.getRegion(adventure.getRaid());
            if (region != null) {
                vec3 = region.getBossPos();
                level = ModDimensions.getRaidDimension(server);
                region.addRegionTicket(level);
            }
            else {
                vec3 = adventure.startPos();
                level = adventure.startDimension();
            }

            PokemonEntity pokemonEntity = boss.getBossEntity(level, null);
            double scaleMod = region != null ? CustomRaidRegistries.RAID_DEN_REGISTRY.getScaleModifier(region.structure()) : 1.0;
            pokemonEntity.getPokemon().setScaleModifier((float) (pokemonEntity.getPokemon().getScaleModifier() * scaleMod));
            if (boss.getNoAi() && adventure.noAI()) pokemonEntity.setNoAi(true);
            pokemonEntity.setInvulnerable(adventure.isInvulnerable());
            if (adventure.isPersistent()) pokemonEntity.setPersistenceRequired();
            ((IRaidAccessor) pokemonEntity).crd_setRaidId(adventure.getRaid());

            this.activeRaid = new RaidInstance(pokemonEntity, adventure.getHost(), adventure.isInDen());
            this.activeRaid.setAdventure(true);
            adventure.setActiveRaid(this.activeRaid);

            if (this.activeRaid.failedToStart()) {
                adventure.stopRaid(false);
                return;
            }

            if (region != null) pokemonEntity.moveTo(region.getBossPos());
            else if (vec3 != null) pokemonEntity.moveTo(vec3);
            level.addFreshEntity(pokemonEntity);

            for (ServerPlayer player : adventure.getPlayers()) {
                this.activeRaid.addPlayer(player);
                this.activeRaid.addToBossEvent(player);
                player.displayClientMessage(ComponentUtils.getSystemMessage(Component.translatable("message.cobblemonraiddens.raid.raid_start", pokemonEntity.getDisplayName())), true);
            }
        }, server);
    }

    @Override
    public boolean isCompleted() {
        return this.isFinished || (this.activeRaid != null && this.activeRaid.isFinished());
    }

    @Override
    public JsonElement serialize(AdventureNode src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", ID);
        JsonArray bosses = new JsonArray();
        for (String reference : this.references) bosses.add(reference);
        object.add("bosses", bosses);
        return object;
    }
}
