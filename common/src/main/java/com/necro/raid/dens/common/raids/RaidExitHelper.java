package com.necro.raid.dens.common.raids;

import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.util.RaidDenRegistry;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RaidExitHelper {
    private static final Set<UUID> RECENT_DEATHS = new HashSet<>();

    public static void afterRespawn(ServerPlayer player) {
        RECENT_DEATHS.add(player.getUUID());
    }

    @SuppressWarnings("unused")
    public static void afterRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        afterRespawn(newPlayer);
    }

    @SuppressWarnings("unused")
    public static void onDimensionChange(ServerPlayer player, ResourceKey<Level> from, ResourceKey<Level> to) {
        if (player.getServer() == null) return;
        ServerLevel fromLevel = player.getServer().getLevel(from);
        if (fromLevel == null) return;
        onDimensionChange(player, fromLevel);
    }

    @SuppressWarnings("unused")
    public static void onDimensionChange(ServerPlayer player, ServerLevel from, ServerLevel to) {
        onDimensionChange(player, from);
    }

    private static void onDimensionChange(ServerPlayer player, ServerLevel from) {
        if (!RaidUtils.isCustomDimension(from)) return;
        else if (RECENT_DEATHS.remove(player.getUUID())) return;

        BlockEntity blockEntity = from.getBlockEntity(BlockPos.ZERO);
        if (!(blockEntity instanceof RaidHomeBlockEntity homeBlock)) return;
        else if (homeBlock.getHomePos() == null || player.getServer() == null) return;
        ServerLevel home = player.getServer().getLevel(homeBlock.getHome());
        if (home == null) return;

        if (home.getBlockEntity(homeBlock.getHomePos()) instanceof RaidCrystalBlockEntity raidCrystalBlockEntity) {
            if (from.getEntitiesOfClass(LivingEntity.class, new AABB(BlockPos.ZERO).inflate(48), RaidExitHelper::isAlive).isEmpty()) raidCrystalBlockEntity.clearRaid();
            else if (from.getEntitiesOfClass(Player.class, new AABB(BlockPos.ZERO).inflate(48)).isEmpty()) raidCrystalBlockEntity.setQueueClose();
            raidCrystalBlockEntity.addChunkTicket();
            raidCrystalBlockEntity.addChunkTicket(BlockPos.containing(RaidDenRegistry.getBossPos(raidCrystalBlockEntity.getRaidStructure())), from);
        }

        RaidDenNetworkMessages.JOIN_RAID.accept(player, false);
    }

    private static boolean isAlive(LivingEntity entity) {
        return entity != null && entity.isAlive();
    }
}
