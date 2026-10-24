package com.necro.raid.dens.common.showdown.loader;

import com.necro.raid.dens.common.CobblemonRaidDens;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

// Implementation of Mega Showdown's Showdown Loader when MSD is not installed.
public abstract class ShowdownLoader {
    protected abstract boolean isMegaShowdownLoaded();

    public void load() {
        if (this.isMegaShowdownLoaded()) return;
        CobblemonRaidDens.LOGGER.info("Initiating showdown files");

        Path showdown = Path.of("./showdown");

        try {
            yoink("assets/cobblemonraiddens/showdown/index.js", showdown.resolve("index.js"));
        } catch (IOException e) {
            CobblemonRaidDens.LOGGER.error("Failed to load showdown files: {}", e.getMessage());
        }
    }

    private void yoink(String resourcePath, Path targetPath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) return;
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
