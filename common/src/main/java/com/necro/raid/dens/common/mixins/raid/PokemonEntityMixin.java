package com.necro.raid.dens.common.mixins.raid;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.DataKeys;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.raids.RaidState;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IShinyRate;
import com.necro.raid.dens.common.registry.RaidRegistry;
import com.necro.raid.dens.common.util.ITransformer;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin extends TamableAnimal implements IRaidAccessor, ITransformer {
    @Shadow(remap = false)
    public abstract void setBattleId(@Nullable UUID value);

    @Shadow(remap = false)
    public abstract Pokemon getPokemon();

    @Unique
    private UUID crd_raidId;

    @Unique
    private ResourceLocation crd_raidBoss;

    @Unique
    private boolean crd_flagRemove = false;

    @Unique
    private RaidState crd_raidState = RaidState.IN_PROGRESS;

    @Unique
    private Pokemon crd_transformTarget = null;

    protected PokemonEntityMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public UUID crd_getRaidId() {
        return this.crd_raidId;
    }

    @Override
    public void crd_setRaidId(UUID raidId) {
        this.crd_raidId = null;
        this.setBattleId(raidId);
        this.crd_raidId = raidId;
    }

    @Override
    public boolean crd_isRaidBoss() {
        return this.getPokemon().getAspects().contains("raid")
            || this.getPokemon().getForcedAspects().contains("raid")
            || this.crd_raidBoss != null;
    }

    @Override
    public RaidBoss crd_getRaidBoss() {
        return RaidRegistry.getRaidBoss(this.crd_raidBoss);
    }

    @Override
    public void crd_setRaidBoss(ResourceLocation raidBoss) {
        this.crd_raidBoss = raidBoss;
    }

    @Override
    public void crd_flagForRemoval() {
        this.crd_flagRemove = true;
    }

    @Override
    public void crd_setRaidState(RaidState state) {
        this.crd_raidState = state;
    }

    @Override
    public RaidState crd_getRaidState() {
        return this.crd_raidState;
    }

    @Override
    public void crd_setTransformTarget(Pokemon pokemon) {
        if (this.crd_transformTarget != null) return;
        this.crd_transformTarget = pokemon.clone(false, null);
        this.crd_transformTarget.setUuid(this.getPokemon().getUuid());
        this.crd_transformTarget.setTetheringId(null);

        Set<String> aspects = new HashSet<>(this.getPokemon().getAspects());
        aspects.add("raid");
        this.getPokemon().setForcedAspects(aspects);
        this.getPokemon().onChange(null);
    }

    @Override
    public Pokemon crd_getTransformTarget() {
        return this.crd_transformTarget;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void tickInject(CallbackInfo ci) {
        if (this.crd_flagRemove) {
            this.discard();
            ci.cancel();
        }
    }

    @Inject(method = "canBattle", at = @At("HEAD"), cancellable = true, remap = false)
    private void canBattleInject(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (this.level().isClientSide()) cir.setReturnValue(true);
        if (this.crd_getRaidId() == null) return;
        else if (this.getHealth() <= 0F || this.isDeadOrDying() || PlayerExtensionsKt.isPartyBusy(player)) cir.setReturnValue(false);
        cir.setReturnValue(true);
    }

    @Inject(method = "setBattleId", at = @At("HEAD"), cancellable = true, remap = false)
    private void setBattleInject(UUID battleId, CallbackInfo ci) {
        if (this.crd_getRaidId() != null) ci.cancel();
    }

    @Inject(method = "isBattling", at = @At("HEAD"), cancellable = true, remap = false)
    private void isBattlingInject(CallbackInfoReturnable<Boolean> cir) {
        if (this.crd_getRaidId() != null) cir.setReturnValue(true);
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void saveWithoutIdInject(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.crd_raidId != null) nbt.putString("raid_id", this.crd_raidId.toString());
        if (this.crd_raidBoss != null) nbt.putString("raid_boss", this.crd_raidBoss.toString());
        if (this.crd_flagRemove) nbt.putBoolean("raid_flag_remove", true);
        if (this.crd_raidState == RaidState.SUCCESS) nbt.putInt("raid_state", 1);
        else if (this.crd_raidState == RaidState.FAILED) nbt.putInt("raid_state", -1);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void loadInject(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("raid_id")) this.crd_raidId = UUID.fromString(nbt.getString("raid_id"));
        if (nbt.contains("raid_boss")) this.crd_raidBoss = ResourceLocation.parse(nbt.getString("raid_boss"));
        this.crd_flagRemove = nbt.contains("raid_flag_remove");
        if (nbt.contains("raid_state")) this.crd_raidState = nbt.getInt("raid_state") == 1 ? RaidState.SUCCESS : RaidState.FAILED;

        if (this.getPokemon() == null) return;
        CompoundTag tag = nbt.getCompound(DataKeys.POKEMON);
        if (tag.contains("raid_shiny_rate")) ((IShinyRate) this.getPokemon()).crd_setRaidShinyRate(tag.getFloat("raid_shiny_rate"));
    }
}
