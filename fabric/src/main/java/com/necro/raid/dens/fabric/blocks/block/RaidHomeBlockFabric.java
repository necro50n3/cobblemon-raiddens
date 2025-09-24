package com.necro.raid.dens.fabric.blocks.block;

import com.mojang.serialization.MapCodec;
import com.necro.raid.dens.common.blocks.block.RaidHomeBlock;
import com.necro.raid.dens.common.network.JoinRaidPacket;
import com.necro.raid.dens.fabric.blocks.entity.RaidHomeBlockEntityFabric;
import com.necro.raid.dens.fabric.network.NetworkMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class RaidHomeBlockFabric extends RaidHomeBlock {
    public static final MapCodec<RaidHomeBlockFabric> CODEC = simpleCodec(RaidHomeBlockFabric::new);

    public RaidHomeBlockFabric(Properties settings) {
        super(settings);
    }

    @Override
    protected @NotNull MapCodec<RaidHomeBlockFabric> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new RaidHomeBlockEntityFabric(blockPos, blockState);
    }

    @Override
    protected void sendClientPacket(ServerPlayer player) {
        NetworkMessages.sendPacketToPlayer(player, new JoinRaidPacket(false));
    }
}
