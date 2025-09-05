package com.necro.raid.dens.neoforge.worldgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.worldgen.ModFeatures;
import com.necro.raid.dens.common.worldgen.RaidDenFeature;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class NeoForgeFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, CobblemonRaidDens.MOD_ID);
    public static final ResourceKey<BiomeModifier> ADD_RAID_DENS = registerKey("add_raid_dens");

    public static void registerFeatures() {
        ModFeatures.RAID_DEN_FEATURE = (Holder<Feature<BlockStateConfiguration>>) (Object) FEATURES.register("raid_den_feature", RaidDenFeature::new);
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name));
    }
}
