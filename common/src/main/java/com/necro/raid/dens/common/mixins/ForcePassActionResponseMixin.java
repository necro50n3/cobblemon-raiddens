package com.necro.raid.dens.common.mixins;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.exception.IllegalActionChoiceException;
import com.necro.raid.dens.common.items.ItemTags;
import com.necro.raid.dens.common.util.IRaidAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ForcePassActionResponse.class)
public abstract class ForcePassActionResponseMixin {
    @Inject(method = "isValid", at = @At("HEAD"), remap = false)
    private void isValidInject(ActiveBattlePokemon activeBattlePokemon, ShowdownMoveset showdownMoveSet, boolean forceSwitch, CallbackInfoReturnable<Boolean> cir) {
        List<ActiveBattlePokemon> targetPokemon = activeBattlePokemon.getActor().getBattle().getSide2().getActivePokemon();
        if (targetPokemon.isEmpty()) return;
        else if (targetPokemon.getFirst() == activeBattlePokemon) return;
        BattlePokemon battlePokemon = targetPokemon.getFirst().getBattlePokemon();
        if (battlePokemon == null) return;
        PokemonEntity pokemonEntity = battlePokemon.getEntity();
        if (pokemonEntity == null) return;
        else if (!((IRaidAccessor) pokemonEntity).isRaidBoss()) return;

        BattleActor battleActor = activeBattlePokemon.getActor();
        ServerPlayer player = battleActor.getBattle().getPlayers().getFirst();
        if (!battleActor.isForPlayer(player)) return;
        if (!player.isCreative()) {
            if (player.getMainHandItem().is(ItemTags.CHEERS)) player.getMainHandItem().grow(1);
            else if (player.getOffhandItem().is(ItemTags.CHEERS)) player.getOffhandItem().grow(1);
        }
        if (!battleActor.getExpectingPassActions().isEmpty()) battleActor.getExpectingPassActions().removeFirst();
        throw new IllegalActionChoiceException(
            battleActor,
            Component.translatable("message.cobblemonraiddens.raid.forbidden_item").getString()
        );
    }
}
