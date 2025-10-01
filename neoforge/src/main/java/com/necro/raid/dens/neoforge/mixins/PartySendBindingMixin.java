package com.necro.raid.dens.neoforge.mixins;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.client.keybind.CobblemonBlockingKeyBinding;
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.blaze3d.platform.InputConstants;
import com.necro.raid.dens.common.network.packets.RaidChallengePacket;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.neoforge.network.NetworkMessages;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PartySendBinding.class)
public abstract class PartySendBindingMixin extends CobblemonBlockingKeyBinding {
    public PartySendBindingMixin(@NotNull String name, @NotNull InputConstants.Type type, int key, @NotNull String category) {
        super(name, type, key, category);
    }

    @Inject(method = "processEntityTarget", at = @At("HEAD"), cancellable = true, remap = false)
    private void processEntityTargetInject(LocalPlayer player, Pokemon pokemon, LivingEntity entity, CallbackInfo ci) {
        if (entity instanceof PokemonEntity pokemonEntity && ((IRaidAccessor) pokemonEntity).isRaidBoss()) {
            if (!pokemonEntity.canBattle(player)) return;
            NetworkMessages.sendPacketToServer(new RaidChallengePacket(pokemonEntity.getId(), pokemon.getUuid(), BattleFormat.Companion.getGEN_9_SINGLES()));
            ci.cancel();
        }
    }
}
