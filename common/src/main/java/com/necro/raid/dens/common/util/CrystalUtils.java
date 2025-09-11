package com.necro.raid.dens.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;

public class CrystalUtils {
    public static boolean hasSkyAccess(LevelReader level, BlockPos blockPos) {
        int topY = level.getChunk(blockPos).getHeight(Heightmap.Types.MOTION_BLOCKING, blockPos.getX() & 15, blockPos.getZ() & 15);
        return blockPos.getY() >= topY;
    }
}
