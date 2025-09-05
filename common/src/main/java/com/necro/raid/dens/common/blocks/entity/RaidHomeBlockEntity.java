package com.necro.raid.dens.common.blocks.entity;

import com.necro.raid.dens.common.dimensions.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public abstract class RaidHomeBlockEntity extends BlockEntity implements GeoBlockEntity {
    private BlockPos homePos;
    private ResourceKey<Level> home;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RaidHomeBlockEntity(BlockEntityType<? extends RaidHomeBlockEntity> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public void setHome(BlockPos homePos, ResourceKey<Level> home) {
        this.homePos = homePos;
        this.home = home;
    }

    public void setHome(BlockPos homePos, ServerLevel level) {
        this.setHome(homePos, level.dimension());
    }

    public BlockPos getHomePos() {
        return this.homePos;
    }

    public ResourceKey<Level> getHome() {
        return this.home;
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        if (!compoundTag.contains("home_pos") || !compoundTag.contains("home_level")) return;
        int[] coords = compoundTag.getIntArray("home_pos");
        this.homePos = new BlockPos(coords[0], coords[1], coords[2]);
        this.home = ModDimensions.createLevelKey(ResourceLocation.parse(compoundTag.getString("home_level")));
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        if (this.homePos == null || this.home == null) return;
        compoundTag.putIntArray("home_pos", List.of(this.homePos.getX(), this.homePos.getY(), this.homePos.getZ()));
        compoundTag.putString("home_level", this.home.location().toString());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }
}
