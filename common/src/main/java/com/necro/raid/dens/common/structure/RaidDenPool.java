package com.necro.raid.dens.common.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class RaidDenPool {
    private final boolean replace;
    private final List<ResourceLocation> dens;

    private ResourceLocation id;

    private RaidDenPool(boolean replace, List<ResourceLocation> dens) {
        this.replace = replace;
        this.dens = new ArrayList<>(dens);

        this.id = null;
    }

    public boolean getReplace() {
        return this.replace;
    }

    public List<ResourceLocation> getDens() {
        return this.dens;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public void addDens(RaidDenPool other) {
        if (other.getReplace()) this.dens.clear();
        this.dens.addAll(other.getDens());
    }

    public static Codec<RaidDenPool> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(RaidDenPool::getReplace),
            ResourceLocation.CODEC.listOf().fieldOf("values").forGetter(RaidDenPool::getDens)
        ).apply(inst, RaidDenPool::new));
    }
}
