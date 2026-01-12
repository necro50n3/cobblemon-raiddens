package com.necro.raid.dens.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name="cobblemonraiddens/conditions")
public class ConditionsConfig implements ConfigData {
    @Comment("List of all conditions to be synced in raid battles. Only edit this file to add custom moves.")
    public String[] primal_weather = { "primordialsea", "desolateland", "deltastream" };
    public String[] screens = { "reflect", "screen", "veil" };
    public String[] tailwind = { "tailwind" };
    @Comment("The only hazards labelled as Hazard in Cobblemon.")
    public String[] hazards = { "spikes", "rock", "web" };
    public String[] terrain = { "electricterrain", "grassyterrain", "mistyterrain", "psychicterrain" };
}
