package com.necro.raid.dens.neoforge.items;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.items.item.CatchingCharmItem;
import com.necro.raid.dens.common.items.item.CheerItem;
import com.necro.raid.dens.common.items.item.RaidPouchItem;
import com.necro.raid.dens.common.items.item.RaidShardItem;
import com.necro.raid.dens.common.showdown.bagitems.CheerBagItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CobblemonRaidDens.MOD_ID);

    public static void registerItems() {
        ModItems.RAID_POUCH = ITEMS.register("raid_pouch", RaidPouchItem::new);
        ModItems.ATTACK_CHEER = ITEMS.register("cheer_attack", () -> new CheerItem(CheerBagItem.CheerType.ATTACK));
        ModItems.DEFENSE_CHEER = ITEMS.register("cheer_defense", () -> new CheerItem(CheerBagItem.CheerType.DEFENSE));
        ModItems.HEAL_CHEER = ITEMS.register("cheer_heal", () -> new CheerItem(CheerBagItem.CheerType.HEAL));
        ModItems.RAID_SHARD = ITEMS.register("raid_shard", RaidShardItem::new);
        ModItems.CATCHING_CHARM = ITEMS.register("catching_charm", CatchingCharmItem::new);
        ModItems.RAIDITE = ITEMS.register("raidite", () -> new Item(new Item.Properties()));
    }
}
