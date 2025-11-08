package com.necro.raid.dens.common.mixins.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TagEntry.class)
public interface TagEntryMixin {
    @Accessor("id")
    ResourceLocation getId();

    @Accessor("tag")
    boolean isTag();
}
