package com.necro.raid.dens.neoforge;

import com.necro.raid.dens.common.compat.ModCompat;
import net.neoforged.fml.ModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CobblemonRaidDensNeoForgeMixinPlugin implements IMixinConfigPlugin {
    private static final Set<String> RCT_MIXINS = Set.of(
        "com.necro.raid.dens.neoforge.mixins.ai.RCTBattleAIMixin"
    );

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (RCT_MIXINS.contains(mixinClassName)) return ModList.get().isLoaded(ModCompat.RCT_API.getModid());
        return true;
    }

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
