package com.necro.raid.dens.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name="cobblemonraiddens/blacklist")
public class BlacklistConfig implements ConfigData {
    @Comment("List of blacklisted Pokemon in raid battles.")
    public String[] pokemon = {};
    @Comment("List of blacklisted abilities in raid battles")
    public String[] abilities = {
        "wonderguard"
    };
    @Comment("List of blacklisted moves in raid battles.")
    public String[] moves = {
        "bestow",
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
