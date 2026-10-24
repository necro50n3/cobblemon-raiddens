package com.necro.raid.dens.common.registry.custom;

import com.necro.raid.dens.common.data.adventure.RaidAdventure;
import com.necro.raid.dens.common.util.DoubleWeightedRandomMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class AdventureRegistry extends CustomRegistry<ResourceLocation, RaidAdventure> {
    private final DoubleWeightedRandomMap<ResourceLocation> weightedMap;

    public AdventureRegistry() {
        super(null);
        this.weightedMap = new DoubleWeightedRandomMap<>();
    }

    @Override
    public void register(ResourceLocation id, RaidAdventure entry) {
        super.register(id, entry);
        this.weightedMap.add(id, entry.instance().getWeight());
    }

    public @Nullable ResourceLocation getRandom(RandomSource random) {
        return this.weightedMap.getRandom(random).orElse(null);
    }
}
