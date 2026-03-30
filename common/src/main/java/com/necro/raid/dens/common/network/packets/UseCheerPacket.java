package com.necro.raid.dens.common.network.packets;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.items.item.CheerItem;
import com.necro.raid.dens.common.network.ServerPacket;
import com.necro.raid.dens.common.showdown.bagitems.CheerBagItem;
import com.necro.raid.dens.common.util.IRaidBattle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record UseCheerPacket(CheerBagItem.CheerType cheerType) implements CustomPacketPayload, ServerPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "use_cheer");
    public static final Type<UseCheerPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, UseCheerPacket> CODEC = StreamCodec.ofMember(UseCheerPacket::write, UseCheerPacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.cheerType);
    }

    public static UseCheerPacket read(FriendlyByteBuf buf) {
        return new UseCheerPacket(buf.readEnum(CheerBagItem.CheerType.class));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        PokemonBattle battle = BattleRegistry.getBattleByParticipatingPlayer(player);
        if (battle == null) return;
        else if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        CheerItem cheerItem = (CheerItem) switch (this.cheerType()) {
            case ATTACK -> ModItems.ATTACK_CHEER.value();
            case DEFENSE -> ModItems.DEFENSE_CHEER.value();
            case HEAL -> ModItems.HEAL_CHEER.value();
        };
        ItemStack cheer = null;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack itemStack = player.getInventory().items.get(i);
            if (itemStack.is(cheerItem)) {
                cheer = itemStack;
                break;
            }
        }
        if (cheer == null) return;
        cheerItem.cheer(player, cheer);
    }
}
