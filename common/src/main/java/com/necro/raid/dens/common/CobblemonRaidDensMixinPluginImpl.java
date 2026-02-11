package com.necro.raid.dens.common;

import com.necro.raid.dens.common.compat.ModCompat;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public abstract class CobblemonRaidDensMixinPluginImpl implements IMixinConfigPlugin {
    protected abstract String mixin(String pkg);

    protected final Map<String, Supplier<Boolean>> MIXINS = Map.of(
        mixin("ai.RCTBattleAIMixin"), () -> this.isModLoaded(ModCompat.RCT_API.getModid()),
        mixin("msd.CobbleEventsMixin"), () -> this.isModLoaded(ModCompat.MEGA_SHOWDOWN.getModid()),
        mixin("showdown.ShowdownInterpreterMixin"), this::isCobblemon171,
        mixin("den.LevelChunkMixin"), () -> !this.isModLoaded("worldedit", "carpet"),
        mixin("den.LevelMixin"), () -> !this.isModLoaded("carpet")
    );

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (MIXINS.containsKey(mixinClassName)) return MIXINS.get(mixinClassName).get();
        return true;
    }

    protected abstract boolean isModLoaded(String... mods);

    protected abstract boolean isCobblemon171();

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
