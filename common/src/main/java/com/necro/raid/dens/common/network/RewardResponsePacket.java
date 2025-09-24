package com.necro.raid.dens.common.network;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.raids.RewardHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record RewardResponsePacket(boolean catchPokemon) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "reward_response");
    public static final Type<RewardResponsePacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RewardResponsePacket> CODEC = StreamCodec.ofMember(RewardResponsePacket::write, RewardResponsePacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.catchPokemon);
    }

    public static RewardResponsePacket read(FriendlyByteBuf buf) {
        return new RewardResponsePacket(buf.readBoolean());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void handleServer(ServerPlayer player) {
        RewardHandler handler = RaidHelper.REWARD_QUEUE.get(player);
        if (handler == null) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.reward.already_received_reward"));
        }
        else if (this.catchPokemon) this.getPokemon(handler, player);
        else this.getItems(handler, player);
    }

    private void getPokemon(RewardHandler handler, ServerPlayer player) {
        if (handler.givePokemonToPlayer()) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.reward.reward_pokemon"));
            RaidHelper.REWARD_QUEUE.remove(player);
        }
    }

    private void getItems(RewardHandler handler, ServerPlayer player) {
        if (handler.giveItemToPlayer()) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.reward.reward_item"));
            RaidHelper.REWARD_QUEUE.remove(player);
        }
    }
}
