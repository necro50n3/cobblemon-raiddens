package com.necro.raid.dens.common.mixins;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor<T> {
    @Accessor("frozen")
    void setFrozen(boolean frozen);

    @Accessor("frozen")
    boolean getFrozen();

    @Accessor("tags")
    Map<TagKey<T>, HolderSet.Named<T>> tags();

    @Accessor("unregisteredIntrusiveHolders")
    @Nullable
    Map<T, Holder.Reference<T>> getUnregisteredIntrusiveHolders();

    @Accessor("toId")
    Reference2IntMap<T> getToId();

    @Accessor("byLocation")
    Map<ResourceLocation, Holder.Reference<T>> getByLocation();

    @Accessor("byKey")
    Map<ResourceKey<T>, Holder.Reference<T>> getByKey();

    @Accessor("byValue")
    Map<T, Holder.Reference<T>> getByValue();

    @Accessor("registrationInfos")
    Map<ResourceKey<T>, RegistrationInfo> getRegistrationInfos();

    @Accessor("registryLifecycle")
    void setRegistryLifecycle(Lifecycle base);
}