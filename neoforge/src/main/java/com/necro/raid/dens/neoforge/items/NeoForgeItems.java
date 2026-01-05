package com.necro.raid.dens.neoforge.items;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.items.item.CheerItem;
import com.necro.raid.dens.common.items.item.RaidPouchItem;
import com.necro.raid.dens.common.showdown.bagitems.CheerBagItem;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgeItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CobblemonRaidDens.MOD_ID);

    public static void registerItems() {
        ModItems.RAID_POUCH = ITEMS.register("raid_pouch", RaidPouchItem::new);
        ModItems.ATTACK_CHEER = ITEMS.register("cheer_attack", () -> new CheerItem(CheerBagItem.CheerType.ATTACK));
        ModItems.DEFENSE_CHEER = ITEMS.register("cheer_defense", () -> new CheerItem(CheerBagItem.CheerType.DEFENSE));
        ModItems.HEAL_CHEER = ITEMS.register("cheer_heal", () -> new CheerItem(CheerBagItem.CheerType.HEAL));
    }

    public static void registerBlockItem(String name, Supplier<BlockItem> blockItem) {
        ITEMS.register(name, blockItem);
    }
}
