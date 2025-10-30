package com.necro.raid.dens.common.mixins.raid;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.DataKeys;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.util.IHealthSetter;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin extends TamableAnimal implements IRaidAccessor {
    @Shadow(remap = false)
    public abstract void setBattleId(@Nullable UUID value);

    @Shadow(remap = false)
    public abstract Pokemon getPokemon();

    @Unique
    private UUID raidId;

    @Unique
    private ResourceLocation raidBoss;

    protected PokemonEntityMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public UUID getRaidId() {
        return this.raidId;
    }

    @Override
    public void setRaidId(UUID raidId) {
        this.raidId = null;
        this.setBattleId(raidId);
        this.raidId = raidId;
    }

    @Override
    public boolean isRaidBoss() {
        return this.getPokemon().getAspects().contains("raid") || this.getPokemon().getForcedAspects().contains("raid");
    }

    @Override
    public RaidBoss getRaidBoss() {
        return RaidRegistry.getRaidBoss(this.raidBoss);
    }

    @Override
    public void setRaidBoss(ResourceLocation raidBoss) {
        this.raidBoss = raidBoss;
    }

    @Inject(method = "canBattle", at = @At("HEAD"), cancellable = true, remap = false)
    private void canBattleInject(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (this.level().isClientSide()) cir.setReturnValue(true);
        if (this.getRaidId() == null) return;
        else if (this.getHealth() <= 0F || this.isDeadOrDying() || PlayerExtensionsKt.isPartyBusy(player)) cir.setReturnValue(false);
        cir.setReturnValue(true);
    }

    @Inject(method = "setBattleId", at = @At("HEAD"), cancellable = true, remap = false)
    private void setBattleInject(UUID battleId, CallbackInfo ci) {
        if (this.getRaidId() != null) ci.cancel();
    }

    @Inject(method = "isBattling", at = @At("HEAD"), cancellable = true, remap = false)
    private void isBattlingInject(CallbackInfoReturnable<Boolean> cir) {
        if (this.getRaidId() != null) cir.setReturnValue(true);
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void saveWithoutIdInject(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.raidId != null) nbt.putString("raid_id", this.raidId.toString());
        if (this.raidBoss != null) nbt.putString("raid_boss", this.raidBoss.toString());
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void loadInject(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("raid_id")) this.raidId = UUID.fromString(nbt.getString("raid_id"));
        if (nbt.contains("raid_boss")) this.raidBoss = ResourceLocation.parse(nbt.getString("raid_boss"));

        if (this.getPokemon() == null) return;
        CompoundTag tag = nbt.getCompound(DataKeys.POKEMON);
        if (tag.contains("max_health_buffer")) ((IHealthSetter) this.getPokemon()).setMaxHealth(tag.getInt("max_health_buffer"));
    }
}
