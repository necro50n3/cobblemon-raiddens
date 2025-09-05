package com.necro.raid.dens.common.network;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.animations.HealthChangeAnimation;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.ClientManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SyncHealthPacket(float healthRatio) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "sync_health");
    public static final Type<SyncHealthPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SyncHealthPacket> CODEC = StreamCodec.ofMember(SyncHealthPacket::write, SyncHealthPacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(this.healthRatio);
    }

    public static SyncHealthPacket read(FriendlyByteBuf buf) {
        return new SyncHealthPacket(buf.readFloat());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void handleClient() {
        ClientManager.RAID_INSTRUCTION_QUEUE.add(this::syncHealth);
    }

    public boolean syncHealth() {
        ClientBattle battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null) return false;
        ClientBattleActor wildActor = battle.getSide2().getActors().getFirst();
        if (wildActor == null) return false;
        wildActor.getActivePokemon().getFirst().getAnimations()
            .add(new HealthChangeAnimation(this.healthRatio, 1f));
        return true;
    }
}
