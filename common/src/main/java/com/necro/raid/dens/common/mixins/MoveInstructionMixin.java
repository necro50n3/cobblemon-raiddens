package com.necro.raid.dens.common.mixins;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.cobblemon.mod.common.battles.interpreter.instructions.MoveInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MoveInstruction.class)
public abstract class MoveInstructionMixin implements InterpreterInstruction {
    @Final
    @Shadow(remap = false)
    private MoveTemplate move;

    @Shadow(remap = false)
    public BattlePokemon userPokemon;

    @Inject(method = "invoke", at = @At("RETURN"), remap = false)
    private void invokeInject(PokemonBattle battle, CallbackInfo ci) {
        if (!((IRaidBattle) battle).isRaidBattle()) return;
        if (this.userPokemon == null || this.userPokemon.getEntity() == null) return;
        else if (((IRaidAccessor) this.userPokemon.getEntity()).isRaidBoss()) return;

        ServerPlayer player = this.userPokemon.getEffectedPokemon().getOwnerPlayer();
        if (player == null) return;

        RaidInstance raid = ((IRaidBattle) battle).getRaidBattle();

        battle.dispatchGo(() -> {
            raid.getPlayers().forEach(p ->
                RaidDenNetworkMessages.RAID_LOG.accept(
                    player,
                    ((TranslatableContents) this.userPokemon.getEffectedPokemon().getDisplayName().getContents()).getKey(),
                    ((TranslatableContents) this.move.getDisplayName().getContents()).getKey()
                )
            );
            return Unit.INSTANCE;
        });
    }
}
