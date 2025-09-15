package com.necro.raid.dens.common.mixins;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.util.IRaidAccessor;
import kotlin.Pair;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(FireworkRocketItem.class)
public class TestItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"))
    private void useOnInject(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getLevel().isClientSide()) return;
        Pair<PokemonBattle, BattleActor> pair = PlayerExtensionsKt.getBattleState((ServerPlayer) context.getPlayer());
        if (pair == null || pair.getFirst() == null) return;

        PokemonBattle battle = pair.getFirst();
        BattlePokemon raidPokemon = battle.getSide2().getActivePokemon().getFirst().getBattlePokemon();
        if (raidPokemon == null || raidPokemon.getEntity() == null || !((IRaidAccessor) raidPokemon.getEntity()).isRaidBoss()) return;

        UUID raidId = ((IRaidAccessor) raidPokemon.getEntity()).getRaidId();
        RaidHelper.ACTIVE_RAIDS.get(raidId).attackCheer();
    }
}
