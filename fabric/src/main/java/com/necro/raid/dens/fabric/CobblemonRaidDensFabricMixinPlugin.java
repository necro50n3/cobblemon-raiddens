package com.necro.raid.dens.fabric;

import com.necro.raid.dens.common.CobblemonRaidDensMixinPluginImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

public class CobblemonRaidDensFabricMixinPlugin extends CobblemonRaidDensMixinPluginImpl {
    @Override
    protected String mixin(String pkg) {
        return "com.necro.raid.dens.fabric.mixins." + pkg;
    }

    @Override
    protected boolean isModAndNewerThan(String mod, String version) {
        return FabricLoader.getInstance().getModContainer(mod)
            .map(container -> {
                try { return container.getMetadata().getVersion().compareTo(Version.parse(version)) >= 0; }
                catch (VersionParsingException e) { throw new RuntimeException(e); }
            })
            .orElse(false);
    }

    @Override
    protected boolean isModAndOlderThan(String mod, String version) {
        return FabricLoader.getInstance().getModContainer(mod)
            .map(container -> {
                try { return container.getMetadata().getVersion().compareTo(Version.parse(version)) <= 0; }
                catch (VersionParsingException e) { throw new RuntimeException(e); }
            })
            .orElse(false);
    }

    @Override
    protected boolean isModLoaded(String... mods) {
        for (String mod : mods) {
            if (FabricLoader.getInstance().isModLoaded(mod)) return true;
        }
        return false;
    }
}
