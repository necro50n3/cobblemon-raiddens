package com.necro.raid.dens.common.mixins.player;

import com.mojang.authlib.GameProfile;
import com.necro.raid.dens.common.util.IRaidTeleporter;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements IRaidTeleporter {
    @Shadow
    @Final
    public MinecraftServer server;
    @Unique
    private Vec3 crd_homePos;

    @Unique
    private ResourceLocation crd_homeLevel;

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Override
    public Vec3 crd_getHomePos() {
        if (this.crd_homePos == null) return this.server.overworld().getSharedSpawnPos().getBottomCenter();
        return this.crd_homePos;
    }

    @Override
    public void crd_setHomePos(Vec3 homePos) {
        this.crd_homePos = homePos;
    }

    @Override
    public ServerLevel crd_getHomeLevel() {
        if (this.crd_homeLevel == null) return this.server.overworld();
        return this.server.getLevel(ResourceKey.create(Registries.DIMENSION, this.crd_homeLevel));
    }

    @Override
    public void crd_setHomeLevel(ResourceLocation homeLevel) {
        this.crd_homeLevel = homeLevel;
    }

    @Override
    public void crd_clearHome() {
        this.crd_homePos = null;
        this.crd_homeLevel = null;
    }

    @Override
    public void crd_returnHome() {
        ServerLevel level = this.crd_getHomeLevel();
        Vec3 pos = this.crd_getHomePos();
        RaidUtils.teleportPlayerSafe((ServerPlayer) (Object) this, level, BlockPos.containing(pos), this.getYHeadRot(), this.getXRot());
        this.crd_clearHome();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    public void addAdditionalSaveDataInject(CompoundTag compoundTag, CallbackInfo ci) {
        if (this.crd_homePos != null) {
            compoundTag.putDouble("crd_pos_x", this.crd_homePos.x());
            compoundTag.putDouble("crd_pos_y", this.crd_homePos.y());
            compoundTag.putDouble("crd_pos_z", this.crd_homePos.z());
        }

        if (this.crd_homeLevel != null) {
            compoundTag.putString("crd_level", this.crd_homeLevel.toString());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    public void readAdditionalSaveDataInject(CompoundTag compoundTag, CallbackInfo ci) {
        if (compoundTag.contains("crdb_pos_x")) {
            double x = compoundTag.getDouble("crd_pos_x");
            double y = compoundTag.getDouble("crd_pos_y");
            double z = compoundTag.getDouble("crd_pos_z");
            this.crd_homePos = new Vec3(x, y, z);
        }

        if (compoundTag.contains("crd_level")) {
            this.crd_homeLevel = ResourceLocation.parse(compoundTag.getString("crd_level"));
        }
    }
}