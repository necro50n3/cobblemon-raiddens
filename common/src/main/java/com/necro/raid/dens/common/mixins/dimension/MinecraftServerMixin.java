package com.necro.raid.dens.common.mixins.dimension;

import com.necro.raid.dens.common.util.ILevelsSetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ILevelsSetter {
    @Final
    @Mutable
    @Shadow
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Final
    @Shadow
    protected LevelStorageSource.LevelStorageAccess storageSource;

    @Override
    public void setLevels(Map<ResourceKey<Level>, ServerLevel> levels) {
        this.levels = levels;
    }

    @Override
    public void deleteLevel(ResourceKey<Level> key) {
        Path worldDir = this.storageSource.getDimensionPath(key);
        if (!worldDir.toFile().exists()) return;
        try { FileUtils.deleteDirectory(worldDir.toFile()); }
        catch (IOException e) {
            throw new RuntimeException("Failed to delete dimension files.", e);
        }
    }
}
