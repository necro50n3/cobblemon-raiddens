package com.necro.raid.dens.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name="cobblemonraiddens/client")
public class ClientConfig implements ConfigData {
    @ConfigEntry.Category("Beacon Beam")
    @Comment("Show Beacon Beam for Tier One Raids")
    public boolean show_beam_tier_one = true;
    @ConfigEntry.Category("Beacon Beam")
    @Comment("Show Beacon Beam for Tier Two Raids")
    public boolean show_beam_tier_two = true;
    @ConfigEntry.Category("Beacon Beam")
    @Comment("Show Beacon Beam for Tier Three Raids")
    public boolean show_beam_tier_three = true;
    @ConfigEntry.Category("Beacon Beam")
    @Comment("Show Beacon Beam for Tier Four Raids")
    public boolean show_beam_tier_four = true;
    @ConfigEntry.Category("Beacon Beam")
    @Comment("Show Beacon Beam for Tier Five Raids")
    public boolean show_beam_tier_five = true;
    @ConfigEntry.Category("Beacon Beam")
    @Comment("Show Beacon Beam for Tier Six Raids")
    public boolean show_beam_tier_six = true;
    @ConfigEntry.Category("Beacon Beam")
    @Comment("Show Beacon Beam for Tier Seven Raids")
    public boolean show_beam_tier_seven = true;
}
