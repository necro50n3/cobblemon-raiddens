package com.necro.raid.dens.fabric.items;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.items.item.CheerItem;
import com.necro.raid.dens.common.items.item.RaidPouchItem;
import com.necro.raid.dens.common.showdown.bagitems.CheerBagItem;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class FabricItems {
    public static void registerItems() {
        ModItems.RAID_POUCH = registerRaidPouch("raid_pouch", new RaidPouchItem());
        ModItems.ATTACK_CHEER = registerRaidPouch("cheer_attack", new CheerItem(CheerBagItem.CheerType.ATTACK));
        ModItems.DEFENSE_CHEER = registerRaidPouch("cheer_defense", new CheerItem(CheerBagItem.CheerType.DEFENSE));
        ModItems.HEAL_CHEER = registerRaidPouch("cheer_heal", new CheerItem(CheerBagItem.CheerType.HEAL));
    }

    private static Holder<Item> registerRaidPouch(String name, Item item) {
        return Registry.registerForHolder(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name),
            item
        );
    }
}
