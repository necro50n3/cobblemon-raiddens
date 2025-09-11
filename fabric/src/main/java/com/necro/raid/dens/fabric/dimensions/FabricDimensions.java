package com.necro.raid.dens.fabric.dimensions;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.dimensions.RaidDenChunkGenerator;
import com.necro.raid.dens.fabric.blocks.FabricBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class FabricDimensions {
    public static void registerChunkGenerator() {
        Registry.register(
            BuiltInRegistries.CHUNK_GENERATOR,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raiddim_chunk"),
            RaidDenChunkGenerator.CODEC
        );
    }

    public static ServerLevel createRaidDimension(MinecraftServer server, String uuid, RaidCrystalBlockEntity blockEntity) {
        ResourceKey<Level> levelKey = ModDimensions.createLevelKey(uuid);

        ServerLevel level = ModDimensions.createRaidDimension(server, levelKey);
        BlockPos zero = new BlockPos(0, 0, 0);
        level.setBlockAndUpdate(zero, FabricBlocks.RAID_HOME_BLOCK.defaultBlockState());
        if (level.getBlockEntity(zero) instanceof RaidHomeBlockEntity homeBlockEntity) {
            homeBlockEntity.setHome(blockEntity.getBlockPos(), (ServerLevel) blockEntity.getLevel());
        }
        DimensionHelper.SYNC_DIMENSIONS.accept(server, levelKey, true);

        ServerWorldEvents.LOAD.invoker().onWorldLoad(server, level);
        return level;
    }
}
