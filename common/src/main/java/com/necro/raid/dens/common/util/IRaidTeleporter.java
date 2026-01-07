package com.necro.raid.dens.common.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public interface IRaidTeleporter {
    Vec3 crd_getHomePos();
    void crd_setHomePos(Vec3 homePos);
    ServerLevel crd_getHomeLevel();
    void crd_setHomeLevel(ResourceLocation level);
    void crd_clearHome();
    void crd_returnHome();
}
