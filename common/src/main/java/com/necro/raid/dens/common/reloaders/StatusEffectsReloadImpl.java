package com.necro.raid.dens.common.reloaders;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StatusEffectsReloadImpl extends AbstractReloadImpl {
    protected final Map<String, String> statuses;

    public StatusEffectsReloadImpl() {
        super("cobblemonraiddens/showdown/conditions", DataType.JAVASCRIPT);
        this.statuses = new HashMap<>();
    }

    @Override
    protected void preLoad() {
        this.statuses.clear();
    }

    @Override
    public void load(@NotNull ResourceManager manager) {
        manager.listResources(this.path, path -> path.toString().endsWith(this.suffix())).forEach((id, resource) -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                String status = reader.lines().collect(Collectors.joining("\n"));
                String statusId = id.getPath().replace(this.idRemove, "").replace(this.suffix(), "");
                this.statuses.put(statusId, status);
            } catch (Exception e) {
                this.onError(id, e);
            }
        });

        this.postLoad();
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load status effect {}", id, e);
    }

    @Override
    protected void postLoad() {}
}
