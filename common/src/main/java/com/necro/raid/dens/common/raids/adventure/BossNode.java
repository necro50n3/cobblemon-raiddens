package com.necro.raid.dens.common.raids.adventure;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.raids.RaidAdventureInstance;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.helpers.RaidRegionHelper;
import com.necro.raid.dens.common.registry.RaidDenRegistry;
import com.necro.raid.dens.common.util.ComponentUtils;
import com.necro.raid.dens.common.util.IRaidAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public class BossNode implements AdventureNode {
    public static final String ID = "boss";

    private transient @Nullable RaidInstance activeRaid;

    @Override
    public int delay() {
        return 40;
    }

    @Override
    public boolean isCompleted() {
        return this.activeRaid != null && this.activeRaid.isFinished();
    }

    @Override
    public void run(RaidAdventureInstance adventure, MinecraftServer server) {
        adventure.schedule(() -> this.startRaid(adventure, server), this.delay(), false);
    }

    private void startRaid(RaidAdventureInstance adventure, MinecraftServer server) {
        RaidBoss boss = adventure.adventure().boss();
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
        double scaleMod = region != null ? RaidDenRegistry.getScaleModifier(region.structure()) : 1.0;
        pokemonEntity.getPokemon().setScaleModifier((float) (pokemonEntity.getPokemon().getScaleModifier() * scaleMod));
        if (boss.getNoAi() && adventure.noAI()) pokemonEntity.setNoAi(true);
        pokemonEntity.setInvulnerable(adventure.isInvulnerable());
        if (adventure.isPersistent()) pokemonEntity.setPersistenceRequired();
        ((IRaidAccessor) pokemonEntity).crd_setRaidId(adventure.getRaid());

        this.activeRaid = new RaidInstance(pokemonEntity, adventure.getHost(), adventure.isInDen());
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
    }

    @Override
    public JsonElement serialize(AdventureNode src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", ID);
        return object;
    }
}
