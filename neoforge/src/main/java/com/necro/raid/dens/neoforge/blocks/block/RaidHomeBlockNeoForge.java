package com.necro.raid.dens.neoforge.blocks.block;

import com.mojang.serialization.MapCodec;
import com.necro.raid.dens.common.blocks.block.RaidHomeBlock;
import com.necro.raid.dens.common.network.packets.JoinRaidPacket;
import com.necro.raid.dens.neoforge.blocks.entity.RaidHomeBlockEntityNeoForge;
import com.necro.raid.dens.neoforge.network.NetworkMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class RaidHomeBlockNeoForge extends RaidHomeBlock {
    public static final MapCodec<RaidHomeBlockNeoForge> CODEC = simpleCodec(RaidHomeBlockNeoForge::new);

    public RaidHomeBlockNeoForge(Properties settings) {
        super(settings);
    }

    @Override
    protected @NotNull MapCodec<RaidHomeBlockNeoForge> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new RaidHomeBlockEntityNeoForge(blockPos, blockState);
    }

    @Override
    protected void sendClientPacket(ServerPlayer player) {
        NetworkMessages.sendPacketToPlayer(player, new JoinRaidPacket(false));
    }
}
