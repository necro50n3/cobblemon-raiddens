package com.necro.raid.dens.common.mixins.client;

import com.cobblemon.mod.common.client.keybind.CobblemonBlockingKeyBinding;
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.blaze3d.platform.InputConstants;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.util.IRaidAccessor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PartySendBinding.class, priority = 1100)
public abstract class PartySendBindingMixin extends CobblemonBlockingKeyBinding {
    public PartySendBindingMixin(@NotNull String name, @NotNull InputConstants.Type type, int key, @NotNull String category) {
        super(name, type, key, category);
    }

    @Inject(method = "processEntityTarget", at = @At("HEAD"), cancellable = true, remap = false)
    private void processEntityTargetInject(LocalPlayer player, Pokemon pokemon, LivingEntity entity, CallbackInfo ci) {
        if (entity instanceof PokemonEntity pokemonEntity && ((IRaidAccessor) pokemonEntity).isRaidBoss()) {
            if (!pokemonEntity.canBattle(player)) return;
            RaidDenNetworkMessages.RAID_CHALLENGE.accept(pokemonEntity, pokemon);
            ci.cancel();
        }
    }
}
