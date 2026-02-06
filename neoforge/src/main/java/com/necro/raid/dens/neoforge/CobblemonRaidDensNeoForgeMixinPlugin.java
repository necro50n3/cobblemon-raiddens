package com.necro.raid.dens.neoforge;

import com.necro.raid.dens.common.compat.ModCompat;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CobblemonRaidDensNeoForgeMixinPlugin implements IMixinConfigPlugin {
    private static final Set<String> RCT_MIXINS = Set.of(
        "com.necro.raid.dens.neoforge.mixins.ai.RCTBattleAIMixin"
    );

    private static final Set<String> MSD_MIXINS = Set.of(
        "com.necro.raid.dens.neoforge.mixins.msd.CobbleEventsMixin"
    );

    private static final Set<String> COBBLEMON_MIXINS = Set.of(
        "com.necro.raid.dens.neoforge.mixins.showdown.ShowdownInterpreterMixin"
    );

    private static final Set<String> ANTI_WORLDEDIT_MIXINS = Set.of(
        "com.necro.raid.dens.neoforge.mixins.den.LevelChunkMixin"
    );

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (RCT_MIXINS.contains(mixinClassName)) return LoadingModList.get().getModFileById(ModCompat.RCT_API.getModid()) != null;
        if (MSD_MIXINS.contains(mixinClassName)) return LoadingModList.get().getModFileById(ModCompat.MEGA_SHOWDOWN.getModid()) != null;
        if (COBBLEMON_MIXINS.contains(mixinClassName)) return CobblemonRaidDensNeoForge.isCobblemon171();
        if (ANTI_WORLDEDIT_MIXINS.contains(mixinClassName)) return LoadingModList.get().getModFileById("worldedit") == null;
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
