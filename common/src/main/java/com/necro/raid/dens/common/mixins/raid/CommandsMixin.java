package com.necro.raid.dens.common.mixins.raid;

import com.mojang.brigadier.ParseResults;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.util.ComponentUtils;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {
    @Inject(method = "performCommand", at = @At("HEAD"), cancellable = true)
    private void performCommand(ParseResults<CommandSourceStack> parseResults, String cmd, CallbackInfo ci) {
        ServerPlayer player = parseResults.getContext().getSource().getPlayer();
        if (player == null || !RaidUtils.isRaidDimension(player.level()) || CobblemonRaidDens.BLACKLIST_CONFIG.commands.length == 0) return;

        String[] parts = cmd.toLowerCase().split(" ");
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, RaidUtils.getMaxCommandSplit()); i++) {
            if (i > 0) prefix.append(" ");
            prefix.append(parts[i]);
            if (RaidUtils.isCommandBlacklisted(prefix.toString())) {
                player.displayClientMessage(ComponentUtils.getErrorMessage("error.cobblemonraiddens.command_blacklist"), true);
                ci.cancel();
            }
        }
    }
}