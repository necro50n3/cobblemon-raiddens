package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMusicPacket;
import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record PlaySoundRaidEvent(ResourceLocation sound, boolean isMusic) implements BroadcastingRaidEvent {
    @Override
    public void broadcast(RaidInstance raid, List<ServerPlayer> players) {
        if (this.isMusic) CobblemonNetwork.sendPacketToPlayers(players, new BattleMusicPacket(this.sound, 1F, 1F, true));
        else raid.getBossEntity().level().playSound(null, raid.getBossEntity().blockPosition(), SoundEvent.createVariableRangeEvent(this.sound), SoundSource.NEUTRAL);
    }

    @Override
    public void run(RaidInstance raid, @Nullable ServerPlayer player) {}
}
