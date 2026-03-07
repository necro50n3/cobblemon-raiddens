package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record RaidEndEvent(ServerPlayer player, RaidBoss raidBoss, @Nullable Pokemon pokemon, float catchRate, boolean isWin) {}
