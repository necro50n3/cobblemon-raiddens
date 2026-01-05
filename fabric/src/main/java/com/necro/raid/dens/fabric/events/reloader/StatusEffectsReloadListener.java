package com.necro.raid.dens.fabric.events.reloader;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownService;
import com.cobblemon.mod.relocations.graalvm.polyglot.Value;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.reloaders.StatusEffectsReloadImpl;
import kotlin.Unit;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class StatusEffectsReloadListener extends StatusEffectsReloadImpl implements SimpleSynchronousResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "showdown/status_effects");
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        this.load(manager);
    }

    @Override
    public void postLoad() {
        Cobblemon.INSTANCE.getShowdownThread().queue(service -> {
            if (!(service instanceof GraalShowdownService graal)) return Unit.INSTANCE;
            Value receiver = graal.getContext().getBindings("js").getMember("receiveConditionData");
            for (Map.Entry<String, String> entry : this.statuses.entrySet()) {
                receiver.execute(entry.getKey(), entry.getValue());
            }
            return Unit.INSTANCE;
        });
    }
}
