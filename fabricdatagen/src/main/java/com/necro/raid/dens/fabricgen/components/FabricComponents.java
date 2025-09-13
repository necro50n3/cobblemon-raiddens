package com.necro.raid.dens.fabricgen.components;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.raids.RaidFeature;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.function.UnaryOperator;

public class FabricComponents {
    @SuppressWarnings("unchecked")
    public static void registerDataComponents() {
        ModComponents.TIER_COMPONENT = Holder.direct((DataComponentType<RaidTier>) registerTierComponent(builder -> builder.persistent(RaidTier.codec())));
        ModComponents.FEATURE_COMPONENT = Holder.direct((DataComponentType<RaidFeature>) registerFeatureComponent(builder -> builder.persistent(RaidFeature.codec())));
        ModComponents.TYPE_COMPONENT = Holder.direct((DataComponentType<RaidType>) registerTypeComponent(builder -> builder.persistent(RaidType.codec())));
    }

    private static DataComponentType<?> registerTierComponent(UnaryOperator<DataComponentType.Builder<RaidTier>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_tier"),
            builderOperator.apply(DataComponentType.builder()).build()
        );
    }

    private static DataComponentType<?> registerFeatureComponent(UnaryOperator<DataComponentType.Builder<RaidFeature>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_feature"),
            builderOperator.apply(DataComponentType.builder()).build()
        );
    }

    private static DataComponentType<?> registerTypeComponent(UnaryOperator<DataComponentType.Builder<RaidType>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_type"),
            builderOperator.apply(DataComponentType.builder()).build()
        );
    }
}
