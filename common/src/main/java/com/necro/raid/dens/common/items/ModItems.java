package com.necro.raid.dens.common.items;

import com.cobblemon.mod.common.item.battle.BagItem;
import com.necro.raid.dens.common.showdown.ClearBoostBagItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

public class ModItems {
    public static Holder<Item> RAID_POUCH;
    public static Holder<Item> ATTACK_CHEER;
    public static Holder<Item> DEFENSE_CHEER;
    public static Holder<Item> HEAL_CHEER;

    public static final BagItem CLEAR_BOOST = new ClearBoostBagItem();
}
