package com.necro.raid.dens.common.mixins;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BattleSide;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.battles.runner.ShowdownService;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.util.IRaidAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(PokemonBattle.class)
public abstract class PokemonBattleMixin {
    @Shadow(remap = false)
    public abstract int getTurn();

    @Shadow(remap = false)
    public abstract BattleSide getSide2();

    @Shadow(remap = false)
    public abstract void log(String string);

    @Shadow(remap = false)
    public abstract UUID getBattleId();

    @Inject(method = "writeShowdownAction", at = @At("HEAD"), remap = false, cancellable = true)
    private void triggerTerastallization(String[] messages, CallbackInfo ci) {
        if (this.getTurn() != 1) return;
        else if (!messages[0].startsWith(">p2")) return;

        UUID raidId;
        try {
            List<ActiveBattlePokemon> activeBattlePokemon = this.getSide2().getActivePokemon();
            BattlePokemon battlePokemon = activeBattlePokemon.getFirst().getBattlePokemon();
            PokemonEntity pokemonEntity = battlePokemon != null ? battlePokemon.getEntity() : null;
            raidId = pokemonEntity != null ? ((IRaidAccessor) pokemonEntity).getRaidId() : null;
        }
        catch (NullPointerException ignored) { return; }
        if (raidId == null) return;
        else if (!RaidHelper.ACTIVE_RAIDS.containsKey(raidId)) return;
        else if (!RaidHelper.ACTIVE_RAIDS.get(raidId).getRaidBoss().isTera()) return;

        this.log(String.join("\n", messages));
        String[] messageList = {messages[0] + " terastal"};
        ShowdownService.Companion.getService().send(this.getBattleId(), messageList);
        ci.cancel();
    }
}
