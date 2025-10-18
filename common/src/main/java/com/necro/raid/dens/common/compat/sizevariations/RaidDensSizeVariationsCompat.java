package com.necro.raid.dens.common.compat.sizevariations;

import com.cobblemon.mod.common.pokemon.Pokemon;
import dev.cudzer.cobblemonsizevariation.CobblemonSizeVariation;
import dev.cudzer.cobblemonsizevariation.network.SizeChangedPacket;
import net.minecraft.server.level.ServerPlayer;

public class RaidDensSizeVariationsCompat {
    public static void setRandomSize(Pokemon pokemon, ServerPlayer player) {
        float size = CobblemonSizeVariation.SIZER.getSize();
        pokemon.setScaleModifier(size);
        if (player != null) CobblemonSizeVariation.platform.getNetworkManager().sendPacketToPlayer(player, new SizeChangedPacket(() -> pokemon, (double)size));
    }
}
