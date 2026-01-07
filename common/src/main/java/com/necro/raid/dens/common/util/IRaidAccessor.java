package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.data.raid.RaidBoss;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface IRaidAccessor {
    UUID crd_getRaidId();
    void crd_setRaidId(UUID raidId);
    RaidBoss crd_getRaidBoss();
    void crd_setRaidBoss(ResourceLocation raidBoss);
    boolean crd_isRaidBoss();
}
