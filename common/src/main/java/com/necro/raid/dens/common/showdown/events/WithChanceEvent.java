package com.necro.raid.dens.common.showdown.events;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record WithChanceEvent(float chance, List<AbstractEvent> events) implements AbstractEvent {
    @Override
    public void execute(RaidContext context) {
        ServerPlayer player = context.player();
        if (player == null) return;
        if (player.getRandom().nextFloat() < this.chance) this.events.forEach(event -> event.execute(context));
    }
}
