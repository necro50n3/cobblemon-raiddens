package com.necro.raid.dens.neoforge.dimensions;

import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

public class NeoForgeDimensions {
    @SuppressWarnings({"deprecation", "ConstantConditions"})
    public static ServerLevel createRaidDimension(RaidCrystalBlockEntity blockEntity) {
        MinecraftServer server = blockEntity.getLevel().getServer();
        ResourceKey<Level> levelKey = ModDimensions.createLevelKey(blockEntity.getRaidHost().toString());

        ServerLevel level = ModDimensions.createRaidDimension(server, levelKey);
        blockEntity.setDimension(level);
        ModDimensions.placeRaidDenStructure(blockEntity);
        DimensionHelper.SYNC_DIMENSIONS.accept(server, levelKey, true);

        server.markWorldsDirty();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(level));
        return level;
    }
}
