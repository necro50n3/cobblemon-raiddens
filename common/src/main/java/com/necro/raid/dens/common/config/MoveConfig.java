package com.necro.raid.dens.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.List;

@Config(name="cobbleraiddens-moves")
public class MoveConfig implements ConfigData {
    @Comment("List of blacklisted moves in raid battles.")
    public String[] blacklist = {
        "circlethrow",
        "destinybond",
        "disable",
        "encore",
        "endeavor",
        "entrainment",
        "fissure",
        "guardsplit",
        "guillotine",
        "horndrill",
        "imprison",
        "instruct",
        "naturesmadness",
        "painsplit",
        "perishsong",
        "powersplit",
        "roar",
        "ruination",
        "sheercold",
        "simplebeam",
        "skillswap",
        "skydrop",
        "superfang",
        "taunt",
        "torment",
        "trick",
        "whirlwind",
        "worryseed"
    };
}
