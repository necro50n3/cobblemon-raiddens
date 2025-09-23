package com.necro.raid.dens.common.network;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidHomeBlock;
import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public record LeaveRaidPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "leave_raid");
    public static final Type<LeaveRaidPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, LeaveRaidPacket> CODEC = StreamCodec.ofMember(LeaveRaidPacket::write, LeaveRaidPacket::read);

    public void write(FriendlyByteBuf buf) {}

    public static LeaveRaidPacket read(FriendlyByteBuf buf) {
        return new LeaveRaidPacket();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void handleServer(ServerPlayer player) {
        assert player.getServer() != null;
        handleDim: {
            if (RaidUtils.isCustomDimension(player.level())) {
                BlockEntity blockEntity = player.level().getBlockEntity(BlockPos.ZERO);

                if (blockEntity instanceof RaidHomeBlockEntity homeBlock) RaidHomeBlock.safeExit(homeBlock, BlockPos.ZERO, player, player.level());
                else {
                    ServerLevel level = player.getServer().overworld();
                    RaidUtils.teleportPlayerSafe(player, level, level.getSharedSpawnPos(), player.getYHeadRot(), player.getXRot());
                }
            }
            else {
                ResourceKey<Level> key = ModDimensions.createLevelKey(String.valueOf(player.getUUID()));
                ServerLevel level = player.getServer().getLevel(key);
                if (level == null || !level.players().isEmpty() || !RaidUtils.isCustomDimension(level)) break handleDim;

                BlockEntity blockEntity = level.getBlockEntity(BlockPos.ZERO);
                if (blockEntity instanceof RaidHomeBlockEntity homeBlock) RaidHomeBlock.safeExit(homeBlock, BlockPos.ZERO, player, player.level());
                else DimensionHelper.queueForRemoval(key, level);
            }
        }

        RaidHelper.removeHost(player.getUUID());
        RaidHelper.removeParticipant(player.getUUID());
    }
}
