package com.necro.raid.dens.common.compat.cobbledollars;

import fr.harmex.cobbledollars.common.utils.CobbleDollarsPlayer;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigInteger;

public class RaidDensCobbleDollarsCompat {
    public static void addCurrency(ServerPlayer player, int amount) {
        ((CobbleDollarsPlayer) player).cobbleDollars$setCobbleDollars(BigInteger.valueOf(amount));
    }
}
