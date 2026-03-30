package com.necro.raid.dens.neoforge;

import com.necro.raid.dens.common.CobblemonRaidDensMixinPluginImpl;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class CobblemonRaidDensNeoForgeMixinPlugin extends CobblemonRaidDensMixinPluginImpl {
    @Override
    protected String mixin(String pkg) {
        return "com.necro.raid.dens.neoforge.mixins." + pkg;
    }

    @Override
    protected boolean isModAndNewerThan(String mod, String version) {
        ModFileInfo info = LoadingModList.get().getModFileById(mod);
        return info.getMods().getFirst().getVersion().compareTo(new DefaultArtifactVersion(version)) >= 0;
    }

    @Override
    protected boolean isModAndOlderThan(String mod, String version) {
        ModFileInfo info = LoadingModList.get().getModFileById(mod);
        return info.getMods().getFirst().getVersion().compareTo(new DefaultArtifactVersion(version)) <= 0;
    }

    @Override
    protected boolean isModLoaded(String... mods) {
        for (String mod : mods) {
            if (LoadingModList.get().getModFileById(mod) != null) return true;
        }
        return false;
    }
}
