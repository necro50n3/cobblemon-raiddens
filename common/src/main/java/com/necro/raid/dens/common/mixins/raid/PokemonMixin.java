package com.necro.raid.dens.common.mixins.raid;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.util.IHealthSetter;
import com.necro.raid.dens.common.util.IShinyRate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pokemon.class)
public abstract class PokemonMixin implements IHealthSetter, IShinyRate {
    @Shadow(remap = false)
    public abstract void setCurrentHealth(int value);

    @Unique
    private Integer maxHealthBuffer;

    @Unique
    private Float raidShinyRate;

    @Override
    public void setMaxHealth(int maxHealth, boolean setCurrent) {
        this.maxHealthBuffer = maxHealth;
        if (setCurrent) this.setCurrentHealth(maxHealth);
    }

    @Override
    public Float getRaidShinyRate() {
        return this.raidShinyRate;
    }

    @Override
    public void setRaidShinyRate(float raidShinyRate) {
        this.raidShinyRate = raidShinyRate;
    }

    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true, remap = false)
    private void getMaxHealthInject(CallbackInfoReturnable<Integer> cir) {
        if (this.maxHealthBuffer != null) cir.setReturnValue(this.maxHealthBuffer);
    }

    @Inject(method = "saveToNBT", at = @At("HEAD"), remap = false)
    private void saveToNBTInject(RegistryAccess registryAccess, CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.maxHealthBuffer != null) nbt.putInt("max_health_buffer", this.maxHealthBuffer);
    }
}
