package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class RaidStructureRegistry {
    private static final Map<ResourceLocation, RaidStructureData> STRUCTURES = new HashMap<>();
    public static final ResourceLocation DEFAULT = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_dens/raid_den");

    public static void register(ResourceLocation structure, CompoundTag tag) {
        STRUCTURES.put(structure, new RaidStructureData(tag));
    }

    public static Vec3 getOffset(ResourceLocation structure) {
        return STRUCTURES.get(structure).offset;
    }

    public static Vec3 getPlayerPos(ResourceLocation structure) {
        return STRUCTURES.get(structure).playerPos;
    }

    public static Vec3 getBossPos(ResourceLocation structure) {
        return STRUCTURES.get(structure).bossPos;
    }

    public static void clear() {
        STRUCTURES.clear();
    }

    private static class RaidStructureData {
        private final Vec3 offset;
        private final Vec3 playerPos;
        private final Vec3 bossPos;

        private RaidStructureData(CompoundTag tag) {
            this.offset = new Vec3(tag.getDouble("offset_x"), tag.getDouble("offset_y"), tag.getDouble("offset_z"));
            this.playerPos = new Vec3(tag.getDouble("player_x"), tag.getDouble("player_y"), tag.getDouble("player_z"));
            this.bossPos = new Vec3(tag.getDouble("boss_x"), tag.getDouble("boss_y"), tag.getDouble("boss_z"));
        }
    }
}
