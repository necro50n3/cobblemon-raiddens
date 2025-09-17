package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.raids.RaidBoss;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface IRaidAccessor {
    UUID getRaidId();

    void setRaidId(UUID raidId);

    RaidBoss getRaidBoss();

    void setRaidBoss(ResourceLocation raidBoss);

    boolean isRaidBoss();
}
