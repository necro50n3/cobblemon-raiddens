package com.necro.raid.dens.neoforge.events.reloader;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownService;
import com.cobblemon.mod.relocations.graalvm.polyglot.Value;
import com.necro.raid.dens.common.reloaders.StatusEffectsReloadImpl;
import kotlin.Unit;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class StatusEffectsReloadListener extends StatusEffectsReloadImpl implements ResourceManagerReloadListener {
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
