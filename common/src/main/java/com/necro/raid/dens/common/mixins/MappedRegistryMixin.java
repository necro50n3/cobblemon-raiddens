package com.necro.raid.dens.common.mixins;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import com.necro.raid.dens.common.util.IRegistryRemover;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements IRegistryRemover {
    @Shadow
    public abstract ResourceKey<? extends Registry<T>> key();

    @Shadow
    @Final
    private ObjectList<Holder.Reference<T>> byId;

    @Override
    public ObjectList<Holder.Reference<T>> getById() {
        return this.byId;
    }

    @Override
    public void removeDimension(ResourceLocation loc) {
        MappedRegistryAccessor<T> accessor = ((MappedRegistryAccessor<T>) this);
        T type = accessor.getByLocation().get(loc).value();
        ObjectList<Holder.Reference<T>> byId = this.getById();
        accessor.getToId().removeInt(loc);

        for (int i = 0; i < byId.size(); i++) {
            Holder.Reference<T> reference = byId.get(i);
            if (reference == null) continue;
            if (!reference.key().location().equals(loc)) continue;
            byId.set(i, null);
            int max = 0;
            for (int j = 0; j < byId.size(); j++) {
                max = byId.get(j) != null ? j : max;
            }
            byId.size(max + 1);
            break;
        }

        accessor.getByLocation().remove(loc);
        accessor.getByKey().remove(ResourceKey.create(this.key(), loc));
        accessor.getByValue().remove(type);
        accessor.getRegistrationInfos().remove(type);
        Lifecycle stable = Lifecycle.stable();
        for (RegistrationInfo regInfo : accessor.getRegistrationInfos().values()) {
            stable.add(regInfo.lifecycle());
        }
        accessor.setRegistryLifecycle(stable);

        for (HolderSet.Named<T> holderSet : accessor.tags().values()) {
            HolderSetNamedAccessor<T> set = (HolderSetNamedAccessor<T>) holderSet;
            ImmutableList.Builder<Holder<T>> list = ImmutableList.builder();
            for (Holder<T> content : set.getContents()) {
                if (!content.is(loc)) list.add(content);
            }
            set.setContents(list.build());
        }
        if (accessor.getUnregisteredIntrusiveHolders() != null) {
            accessor.getUnregisteredIntrusiveHolders().remove(type);
        }
    }
}
