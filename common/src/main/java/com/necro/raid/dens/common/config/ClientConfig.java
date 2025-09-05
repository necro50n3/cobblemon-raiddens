package com.necro.raid.dens.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name="cobbleraiddens-client")
public class ClientConfig implements ConfigData {
    @ConfigEntry.Category("Rendering")
    @Comment("Show beacon beams over raid dens. Default: true")
    public boolean show_beam = true;
}
