package com.necro.raid.dens.common.mixins.dimension;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(HolderSet.Named.class)
public interface HolderSetNamedAccessor<T> {
    @Accessor("contents")
    List<Holder<T>> getContents();

    @Accessor("contents")
    void setContents(List<Holder<T>> contents);
}
