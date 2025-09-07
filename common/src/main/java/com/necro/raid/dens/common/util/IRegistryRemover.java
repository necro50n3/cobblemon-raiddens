package com.necro.raid.dens.common.util;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

public interface IRegistryRemover<T> {
    void removeDimension(ResourceLocation loc);

    ObjectList<Holder.Reference<T>> getById();
}
