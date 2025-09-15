package com.necro.raid.dens.fabric.items;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.items.item.CheerItem;
import com.necro.raid.dens.common.items.item.RaidPouchItem;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class FabricItems {
    public static void registerItems() {
        ModItems.RAID_POUCH = registerRaidPouch("raid_pouch", new RaidPouchItem());
        ModItems.ATTACK_CHEER = registerRaidPouch("cheer_attack", new CheerItem(CheerItem.CheerType.ATTACK, 1));
        ModItems.DEFENSE_CHEER = registerRaidPouch("cheer_defense", new CheerItem(CheerItem.CheerType.DEFENSE, 1));
        ModItems.HEAL_CHEER = registerRaidPouch("cheer_heal", new CheerItem(CheerItem.CheerType.HEAL, 0.3));
    }

    private static Holder<Item> registerRaidPouch(String name, Item item) {
        return Registry.registerForHolder(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name),
            item
        );
    }
}
