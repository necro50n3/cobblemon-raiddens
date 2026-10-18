package com.necro.raid.dens.common.raids.adventure;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import com.necro.raid.dens.common.data.raid.RaidBucket;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.RaidAdventureInstance;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.helpers.RaidRegionHelper;
import com.necro.raid.dens.common.registry.RaidBucketRegistry;
import com.necro.raid.dens.common.registry.RaidDenRegistry;
import com.necro.raid.dens.common.registry.RaidRegistry;
import com.necro.raid.dens.common.util.ComponentUtils;
import com.necro.raid.dens.common.util.DoubleWeightedRandomMap;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

public class RaidNode implements AdventureNode {
    public static final String ID = "raid";

    private final List<String> references;
    private transient @Nullable RaidInstance activeRaid = null;
    private transient boolean waitingForChoice = false;
    private transient boolean choiceMade = false;
    private transient @Nullable ResourceLocation option1 = null;
    private transient @Nullable ResourceLocation option2 = null;
    private transient @Nullable UUID choosingPlayerUuid = null;

    public RaidNode(Collection<String> references) {
        this.references = new ArrayList<>(references);
    }

    public List<String> getReferences() {
        return this.references;
    }

    public @Nullable RaidInstance getActiveRaid() {
        return this.activeRaid;
    }

    public boolean isWaitingForChoice() {
        return this.waitingForChoice;
    }

    public @Nullable ResourceLocation getOption1() {
        return this.option1;
    }

    public @Nullable ResourceLocation getOption2() {
        return this.option2;
    }

    public List<ResourceLocation> resolveBosses() {
        LinkedHashSet<ResourceLocation> bosses = new LinkedHashSet<>();
        for (String ref : this.references) {
            if (ref == null || ref.isEmpty()) continue;
            if (ref.startsWith("@")) {
                ResourceLocation bucketId = ResourceLocation.parse(ref.substring(1));
                RaidBucket bucket = RaidBucketRegistry.getBucket(bucketId);
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
        if (resolved.isEmpty()) return;

        if (resolved.size() == 1) this.startRaid(resolved.getFirst(), adventure, server);
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

            RaidRegistry.requestRaidBoss(this.option1);
            RaidRegistry.requestRaidBoss(this.option2);

            ServerPlayer selectingPlayer = adventure.getSelectingPlayer(server);
            if (selectingPlayer == null) {
                this.startRaid(this.option1, adventure, server);
                return;
            }

            this.choosingPlayerUuid = selectingPlayer.getUUID();
            this.waitingForChoice = true;
            this.choiceMade = false;

            RaidDenNetworkMessages.CHOOSE_RAID_BOSS.accept(selectingPlayer, adventure.getRaid(), this.option1, this.option2);

            adventure.schedule(() -> {
                if (this.waitingForChoice && !this.choiceMade) this.selectBoss(this.option1, adventure, server);
            }, 300, false);
        }
    }

    public void onBossSelected(ServerPlayer player, ResourceLocation selectedBoss, RaidAdventureInstance adventure, MinecraftServer server) {
        if (!this.waitingForChoice || this.choiceMade) return;
        if (this.choosingPlayerUuid != null && !this.choosingPlayerUuid.equals(player.getUUID())) return;
        if (selectedBoss == null || (!selectedBoss.equals(this.option1) && !selectedBoss.equals(this.option2))) selectedBoss = this.option1;
        this.selectBoss(selectedBoss, adventure, server);
    }

    public void selectBoss(ResourceLocation selected, RaidAdventureInstance adventure, MinecraftServer server) {
        if (this.choiceMade) return;
        this.choiceMade = true;
        this.waitingForChoice = false;
        this.startRaid(selected, adventure, server);
    }

    public void startRaid(ResourceLocation bossId, RaidAdventureInstance adventure, MinecraftServer server) {
        RaidRegistry.requestRaidBoss(bossId, boss -> {
            ServerLevel level = ModDimensions.getRaidDimension(server);
            RaidRegion region = RaidRegionHelper.getRegion(adventure.getRaid());
            if (region != null) region.addRegionTicket(level);

            PokemonEntity pokemonEntity = boss.getBossEntity(level, null);
            double scaleMod = region != null ? RaidDenRegistry.getScaleModifier(region.structure()) : 1.0;
            pokemonEntity.getPokemon().setScaleModifier((float) (pokemonEntity.getPokemon().getScaleModifier() * scaleMod));
            if (boss.getNoAi()) pokemonEntity.setNoAi(true);
            pokemonEntity.setInvulnerable(true);
            pokemonEntity.setPersistenceRequired();
            ((IRaidAccessor) pokemonEntity).crd_setRaidId(adventure.getRaid());

            this.activeRaid = new RaidInstance(pokemonEntity, adventure.getHost(), adventure.isInDen());
            adventure.setActiveRaid(this.activeRaid);

            if (this.activeRaid.failedToStart()) {
                adventure.stopRaid(false);
                return;
            }

            if (region != null) pokemonEntity.moveTo(region.getBossPos());
            else if (adventure.startPos() != null) pokemonEntity.moveTo(adventure.startPos().getBottomCenter());
            level.addFreshEntity(pokemonEntity);

            for (ServerPlayer player : adventure.getPlayers()) {
                this.activeRaid.addPlayer(player);
                if (region != null) RaidUtils.teleportPlayerToRaid(player, server, region);
                this.activeRaid.addToBossEvent(player);
                player.displayClientMessage(ComponentUtils.getSystemMessage(Component.translatable("message.cobblemonraiddens.raid.raid_start", pokemonEntity.getDisplayName())), true);
            }
        }, server);
    }

    @Override
    public boolean isCompleted() {
        return this.activeRaid != null && this.activeRaid.isFinished();
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
