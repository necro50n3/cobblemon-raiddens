package com.necro.raid.dens.common.showdown;

import com.cobblemon.mod.common.item.battle.BagItem;
import com.necro.raid.dens.common.showdown.bagitems.*;

public class RaidBagItems {
    public static final BagItem CLEAR_BOSS = new ClearBoostBagItem(ClearBoostBagItem.ClearType.BOSS);
    public static final BagItem CLEAR_PLAYER = new ClearBoostBagItem(ClearBoostBagItem.ClearType.PLAYER);

    public static final BagItem SET_RAIN = new SetWeatherBagItem("raindance");
    public static final BagItem SET_SANDSTORM = new SetWeatherBagItem("sandstorm");
    public static final BagItem SET_SNOW = new SetWeatherBagItem("snow");
    public static final BagItem SET_SUN = new SetWeatherBagItem("sunnyday");

    public static final BagItem SET_ELECTRIC_TERRAIN = new SetTerrainBagItem("electricterrain");
    public static final BagItem SET_GRASSY_TERRAIN = new SetTerrainBagItem("grassyterrain");
    public static final BagItem SET_MISTY_TERRAIN = new SetTerrainBagItem("mistyterrain");
    public static final BagItem SET_PSYCHIC_TERRAIN = new SetTerrainBagItem("psychicterrain");
}
