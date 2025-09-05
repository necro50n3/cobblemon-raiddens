package com.necro.raid.dens.neoforge.dimensions;

import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.neoforge.network.NetworkMessages;
import com.necro.raid.dens.common.network.SyncRaidDimensionsPacket;
import com.necro.raid.dens.neoforge.blocks.NeoForgeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

public class NeoForgeDimensions {


    public static ServerLevel createRaidDimension(MinecraftServer server, String uuid, RaidCrystalBlockEntity blockEntity) {
        ResourceKey<Level> levelKey = ModDimensions.createLevelKey(uuid);

        ServerLevel level = ModDimensions.createRaidDimension(server, levelKey);
        BlockPos zero = new BlockPos(0, 0, 0);
        level.setBlockAndUpdate(zero, NeoForgeBlocks.RAID_HOME_BLOCK.get().defaultBlockState());
        if (level.getBlockEntity(zero) instanceof RaidHomeBlockEntity homeBlockEntity) {
            homeBlockEntity.setHome(blockEntity.getBlockPos(), (ServerLevel) blockEntity.getLevel());
        }
        NetworkMessages.sendPacketToAll(new SyncRaidDimensionsPacket(levelKey, true));

        server.markWorldsDirty();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(level));
        return level;
    }
}
