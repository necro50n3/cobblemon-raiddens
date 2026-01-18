package com.necro.raid.dens.common.mixins;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.command.SpawnPokemon;
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnPokemon.class)
public class SpawnPokemonMixin {
    @Inject(method = "execute", at = @At("HEAD"), remap = false)
    private void executeInject(CommandContext<CommandSourceStack> context, Vec3 pos, CallbackInfoReturnable<Integer> cir) {
        PokemonProperties properties = PokemonPropertiesArgumentType.Companion.getPokemonProperties(context, "properties");
        CobblemonRaidDens.LOGGER.info(properties.saveToJSON().toString());
    }
}
