package com.necro.raid.dens.common.registry.custom;

import com.necro.raid.dens.common.data.adventure.RaidAdventure;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.util.DoubleWeightedRandomMap;
import kotlin.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AdventureRegistry extends CustomRegistry<ResourceLocation, RaidAdventure> {
    private final DoubleWeightedRandomMap<ResourceLocation> weightedMap;
    private final Map<ResourceLocation, RaidTier> tierMap;
    private final Map<ResourceLocation, RaidType> typeMap;

    public AdventureRegistry() {
        super(null);
        this.weightedMap = new DoubleWeightedRandomMap<>();
        this.tierMap = new HashMap<>();
        this.typeMap = new HashMap<>();
    }

    public Pair<RaidTier, RaidType> getTierAndType(ResourceLocation entry) {
        if (!this.containsKey(entry)) return null;
        return new Pair<>(this.tierMap.get(entry), this.typeMap.get(entry));
    }

    @Override
    public void register(ResourceLocation id, RaidAdventure entry) {
        super.register(id, entry);
        this.weightedMap.add(id, entry.instance().getWeight());
        this.tierMap.put(id, entry.instance().getTier());
        this.typeMap.put(id, entry.instance().getType());
    }

    public @Nullable ResourceLocation getRandom(RandomSource random) {
        return this.weightedMap.getRandom(random).orElse(null);
    }

    @Override
    public void clear() {
        super.clear();
        this.tierMap.clear();
        this.typeMap.clear();
    }
}
