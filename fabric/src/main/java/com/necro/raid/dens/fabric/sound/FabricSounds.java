package com.necro.raid.dens.fabric.sound;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.sound.ModSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class FabricSounds {
    public static void registerSounds() {
        ModSounds.RAID_DEFAULT = register("battle.raid.default");
        ModSounds.RAID_TIER_ONE = register("battle.raid.tier_one");
        ModSounds.RAID_TIER_TWO = register("battle.raid.tier_two");
        ModSounds.RAID_TIER_THREE = register("battle.raid.tier_three");
        ModSounds.RAID_TIER_FOUR = register("battle.raid.tier_four");
        ModSounds.RAID_TIER_FIVE = register("battle.raid.tier_five");
        ModSounds.RAID_TIER_SIX = register("battle.raid.tier_six");
        ModSounds.RAID_TIER_SEVEN = register("battle.raid.tier_seven");
    }

    private static Holder<SoundEvent> register(String name) {
        ResourceLocation sound = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name);
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, sound, SoundEvent.createVariableRangeEvent(sound));
    }
}
