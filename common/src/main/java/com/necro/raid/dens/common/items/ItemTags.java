package com.necro.raid.dens.common.items;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ItemTags {
    public static final TagKey<Item> RAID_DEN_KEY = createTag("raid_den_key");

    public static TagKey<Item> createTag(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name));
    }
}
