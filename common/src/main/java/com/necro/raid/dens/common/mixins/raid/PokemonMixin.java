package com.necro.raid.dens.common.mixins.raid;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.util.IShinyRate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pokemon.class)
public abstract class PokemonMixin implements IShinyRate {

    @Unique
    private Float raidShinyRate;

    @Override
    public Float getRaidShinyRate() {
        return this.raidShinyRate;
    }

    @Override
    public void setRaidShinyRate(float raidShinyRate) {
        this.raidShinyRate = raidShinyRate;
    }

    @Inject(method = "saveToNBT", at = @At("HEAD"), remap = false)
    private void saveToNBTInject(RegistryAccess registryAccess, CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.raidShinyRate != null) nbt.putFloat("raid_shiny_rate", this.raidShinyRate);
    }
}
