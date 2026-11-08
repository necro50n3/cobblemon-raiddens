package com.necro.raid.dens.common.mixins.player;

import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.raids.RaidSession;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerExtensionsKt.class)
public class PlayerExtensionsKtMixin {
    @Inject(method = "isInBattle", at = @At("HEAD"), remap = false, cancellable = true)
    private static void isInBattleInject(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        RaidJoinHelper.Participant participant = RaidJoinHelper.getParticipant(player);
        if (participant == null) return;
        RaidSession session = RaidHelper.ACTIVE_RAIDS.get(participant.raid());
        if (session != null && !session.canUsePC(player)) cir.setReturnValue(true);
    }
}
