package com.necro.raid.dens.common.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Map;

public interface ILevelsSetter {
    void setLevels(Map<ResourceKey<Level>, ServerLevel> levels);

    void deleteLevel(ResourceKey<Level> level);
}
