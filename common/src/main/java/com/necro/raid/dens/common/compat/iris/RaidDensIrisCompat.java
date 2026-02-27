package com.necro.raid.dens.common.compat.iris;

import net.irisshaders.iris.api.v0.IrisApi;

public class RaidDensIrisCompat {
    public static boolean isEnabled() {
        return IrisApi.getInstance().isShaderPackInUse();
    }
}
