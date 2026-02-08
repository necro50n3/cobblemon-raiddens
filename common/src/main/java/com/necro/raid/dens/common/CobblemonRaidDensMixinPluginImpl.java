package com.necro.raid.dens.common;

import com.necro.raid.dens.common.compat.ModCompat;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public abstract class CobblemonRaidDensMixinPluginImpl implements IMixinConfigPlugin {
    protected abstract String mixin(String pkg);

    protected final Set<String> RCT_MIXINS = Set.of(
        mixin("ai.RCTBattleAIMixin")
    );

    protected final Set<String> MSD_MIXINS = Set.of(
        mixin("msd.CobbleEventsMixin")
    );

    protected final Set<String> COBBLEMON_MIXINS = Set.of(
        mixin("showdown.ShowdownInterpreterMixin")
    );

    protected final Set<String> ANTI_WORLDEDIT_MIXINS = Set.of(
        mixin("den.LevelChunkMixin")
    );

    protected final Set<String> ANTI_CARPET_MIXINS = Set.of(
        mixin("den.LevelMixin"),
        mixin("den.LevelChunkMixin")
    );

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (RCT_MIXINS.contains(mixinClassName)) return this.isModLoaded(ModCompat.RCT_API.getModid());
        if (MSD_MIXINS.contains(mixinClassName)) return this.isModLoaded(ModCompat.MEGA_SHOWDOWN.getModid());
        if (COBBLEMON_MIXINS.contains(mixinClassName)) return this.isCobblemon171();
        if (ANTI_WORLDEDIT_MIXINS.contains(mixinClassName)) return !this.isModLoaded("worldedit");
        if (ANTI_CARPET_MIXINS.contains(mixinClassName)) return !this.isModLoaded("carpet");
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
