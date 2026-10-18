package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.data.RaidSession;
import com.necro.raid.dens.common.raids.RaidState;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface IRaidAccessor {
    UUID crd_getRaidId();
    void crd_setRaidId(UUID raidId);
    RaidSession crd_getRaidSession();
    ResourceLocation crd_getRaidSessionId();
    void crd_setRaidSession(ResourceLocation raid);
    boolean crd_isRaidBoss();
    void crd_flagForRemoval();
    void crd_setRaidState(RaidState state);
    RaidState crd_getRaidState();
    void crd_setShouldRenderHpBar(boolean render);
    boolean crd_shouldRenderHpBar();
}
