package com.necro.raid.dens.common.items;

import com.cobblemon.mod.common.item.battle.BagItem;
import com.necro.raid.dens.common.showdown.bagitems.ClearBoostBagItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

public class ModItems {
    public static Holder<Item> RAID_POUCH;
    public static Holder<Item> ATTACK_CHEER;
    public static Holder<Item> DEFENSE_CHEER;
    public static Holder<Item> HEAL_CHEER;

    public static final BagItem CLEAR_BOSS = new ClearBoostBagItem(ClearBoostBagItem.ClearType.BOSS);
    public static final BagItem CLEAR_PLAYER = new ClearBoostBagItem(ClearBoostBagItem.ClearType.PLAYER);
}
