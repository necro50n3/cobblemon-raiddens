package com.necro.raid.dens.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashSet;

public class TeleportUtils {
    public static void teleportPlayerSafe(Player player, ServerLevel level, BlockPos targetPos, float yaw, float pitch) {
        BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, targetPos);

        if (TeleportUtils.isSafe(level, groundPos.north())) {
            player.teleportTo(level, groundPos.getX() + 0.5, groundPos.getY(), groundPos.getZ() - 0.5,
                new HashSet<>(), yaw, pitch);
            return;
        }

        int radius = 1;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, targetPos.offset(dx, 0, dz));
                if (TeleportUtils.isSafe(level, pos)) {
                    player.teleportTo(level,pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                        new HashSet<>(), yaw, pitch);
                    return;
                }
            }
        }

        player.teleportTo(level, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() - 0.5,
            new HashSet<>(), yaw, pitch);
    }

    private static boolean isSafe(ServerLevel world, BlockPos pos) {
        BlockState block = world.getBlockState(pos);
        BlockState above = world.getBlockState(pos.above());

        return !block.is(Blocks.LAVA) &&
            block.getCollisionShape(world, pos).isEmpty() &&
            above.getCollisionShape(world, pos.above()).isEmpty();
    }
}
