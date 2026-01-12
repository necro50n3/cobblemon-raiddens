package com.necro.raid.dens.common.mixins.showdown;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.cobblemon.mod.common.battles.interpreter.instructions.MoveInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.data.support.RaidSupport;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.registry.RaidSupportRegistry;
import com.necro.raid.dens.common.showdown.events.DragonCheerShowdownEvent;
import com.necro.raid.dens.common.showdown.events.RaidCureShowdownEvent;
import com.necro.raid.dens.common.showdown.events.RaidHealShowdownEvent;
import com.necro.raid.dens.common.showdown.events.RaidSupportShowdownEvent;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Unit;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
        if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        if (this.userPokemon == null || this.userPokemon.getEntity() == null) return;
        else if (((IRaidAccessor) this.userPokemon.getEntity()).crd_isRaidBoss()) return;

        ServerPlayer player = this.userPokemon.getEffectedPokemon().getOwnerPlayer();
        if (player == null) return;

        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();

        battle.dispatchGo(() -> {
            ComponentContents pokemonContents = this.userPokemon.getEffectedPokemon().getDisplayName(false).getContents();
            ComponentContents moveContents = this.move.getDisplayName().getContents();

            raid.getPlayers().forEach(p ->
                RaidDenNetworkMessages.RAID_LOG.accept(
                    p,
                    this.crd_resolveContents(pokemonContents),
                    this.crd_resolveContents(moveContents)
                )
            );

            if (!raid.canSync()) return Unit.INSTANCE;

            String moveId = this.move.getName();
            RaidSupport support = RaidSupportRegistry.getSupport(moveId);
            if (support != null) {
                support.boosts().forEach((stat, stages) ->
                    raid.broadcastToOthers(new RaidSupportShowdownEvent(moveId, stat, stages), battle)
                );
                if (support.heal() > 0) raid.broadcastToOthers(new RaidHealShowdownEvent(support.heal()), battle);
                if (support.cure()) raid.broadcastToOthers(new RaidCureShowdownEvent(), battle);
                if (support.sendUpdate()) RaidSupportRegistry.addToQueue(this.userPokemon.getUuid());
            }
            else {
                if (moveId.equals("dragoncheer")) {
                    raid.broadcastToOthers(new DragonCheerShowdownEvent(), battle);
                    RaidSupportRegistry.addToQueue(this.userPokemon.getUuid());
                }
            }
            battle.dispatch(() -> {
                RaidSupportRegistry.removeFromQueue(this.userPokemon.getUuid());
                return DispatchResultKt.getGO();
            });

            return Unit.INSTANCE;
        });
    }

    @Unique
    private String crd_resolveContents(ComponentContents contents) {
        if (contents instanceof TranslatableContents t) return t.getKey();
        else if (contents instanceof PlainTextContents p) return p.text();
        else return contents.toString();
    }
}
