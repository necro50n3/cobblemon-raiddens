package com.necro.raid.dens.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name="cobblemonraiddens/client")
public class ClientConfig implements ConfigData {
    @ConfigEntry.Category("beacon_beam")
    @ConfigEntry.BoundedDiscrete(max=255)
    @Comment("Beacon Beam Strength for Tier One Raids")
    public int beam_strength_tier_one = 32;
    @ConfigEntry.Category("beacon_beam")
    @ConfigEntry.BoundedDiscrete(max=255)
    @Comment("Beacon Beam Strength for Tier Two Raids")
    public int beam_strength_tier_two = 64;
    @ConfigEntry.Category("beacon_beam")
    @ConfigEntry.BoundedDiscrete(max=255)
    @Comment("Beacon Beam Strength for Tier Three Raids")
    public int beam_strength_tier_three = 96;
    @ConfigEntry.Category("beacon_beam")
    @ConfigEntry.BoundedDiscrete(max=255)
    @Comment("Beacon Beam Strength for Tier Four Raids")
    public int beam_strength_tier_four = 128;
    @ConfigEntry.Category("beacon_beam")
    @ConfigEntry.BoundedDiscrete(max=255)
    @Comment("Beacon Beam Strength for Tier Five Raids")
    public int beam_strength_tier_five = 160;
    @ConfigEntry.Category("beacon_beam")
    @ConfigEntry.BoundedDiscrete(max=255)
    @Comment("Beacon Beam Strength for Tier Six Raids")
    public int beam_strength_tier_six = 192;
    @ConfigEntry.Category("beacon_beam")
    @ConfigEntry.BoundedDiscrete(max=255)
    @Comment("Beacon Beam Strength for Tier Seven Raids")
    public int beam_strength_tier_seven = 224;
    @ConfigEntry.Category("beacon_beam")
    @Comment("Render the raid crystal particle effects")
    public boolean show_particles = true;
    @ConfigEntry.Category("beacon_beam")
    @Comment("Render the legacy beacon effect")
    public boolean show_legacy_beacon = false;

    @ConfigEntry.Category("raiding")
    @Comment("Automatically accept raid join requests")
    public boolean auto_accept_requests = false;
    @ConfigEntry.Category("raiding")
    @Comment("Show raid logs during raids")
    public boolean enable_raid_logs = true;
    @ConfigEntry.Category("raiding")
    @Comment("Show health bars above Pokemon during raids")
    public boolean enable_health_bars = true;

    @ConfigEntry.Category("gui")
    @ConfigEntry.BoundedDiscrete(max=100)
    public int raid_status_x = 100;
    @ConfigEntry.Category("gui")
    @ConfigEntry.BoundedDiscrete(max=100)
    public int raid_status_y = 50;
    @ConfigEntry.Category("gui")
    @ConfigEntry.BoundedDiscrete(max=100)
    public int raid_popup_x = 50;
    @ConfigEntry.Category("gui")
    @ConfigEntry.BoundedDiscrete(max=100)
    public int raid_popup_y = 77;
}
