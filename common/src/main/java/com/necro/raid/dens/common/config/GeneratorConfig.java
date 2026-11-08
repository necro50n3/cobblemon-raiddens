package com.necro.raid.dens.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name="cobblemonraiddens/generator")
public class GeneratorConfig implements ConfigData {
    @Comment(
        """
        Whether the auto raid generator is enabled.
        All raids generated this way will be registered under the namespace "cobblemonraiddens:<species>".
        Pokemon with no valid attacking moves in their level learnset will be ignored.
        """
    )
    public boolean enabled = false;

    @Comment(
        """
        A list of Pokémon species to whitelist from encounters.
        If this list is not empty, all other config filters are ignored.
        """
    )
    public String[] pokemon_whitelist = {};
    @Comment("A list of Pokémon species to blacklist from encounters.")
    public String[] pokemon_blacklist = {};
    @Comment("A list of Pokémon labels to blacklist from encounters.")
    public String[] label_blacklist = { "legendary", "mythical", "ultra_beast", "paradox" };

    @Comment("Tier definitions based on a Pokémon's Base Stat Total.")
    public int tier_one_bst_threshold = 0;
    public int tier_two_bst_threshold = 256;
    public int tier_three_bst_threshold = 321;
    public int tier_four_bst_threshold = 421;
    public int tier_five_bst_threshold = 511;
}
