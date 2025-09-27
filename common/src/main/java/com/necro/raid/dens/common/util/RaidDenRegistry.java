package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.structure.RaidDenPool;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaidDenRegistry {
    public static final ResourceKey<Registry<RaidDenPool>> DEN_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("raid", "den_pool"));

    private static final Map<ResourceLocation, RaidDenPool> DEN_POOL = new HashMap<>();

    private static final Map<ResourceLocation, RaidStructureData> TEMPLATES = new HashMap<>();
    public static final ResourceLocation DEFAULT = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den/basic");

    public static void register(RaidDenPool pool) {
        if (DEN_POOL.containsKey(pool.getId())) DEN_POOL.get(pool.getId()).addDens(pool);
        else DEN_POOL.put(pool.getId(), pool);
    }

    public static void register(ResourceLocation structure, CompoundTag tag) {
        TEMPLATES.put(structure, new RaidStructureData(tag));
    }

    public static List<ResourceLocation> getStructures(ResourceLocation pool) {
        if (!DEN_POOL.containsKey(pool)) return List.of();
        else return DEN_POOL.get(pool).getDens();
    }

    public static boolean isNotValidStructure(ResourceLocation structure) {
        return !TEMPLATES.containsKey(structure);
    }

    public static Vec3 getOffset(ResourceLocation structure) {
        if (!TEMPLATES.containsKey(structure)) return TEMPLATES.get(DEFAULT).offset;
        return TEMPLATES.get(structure).offset;
    }

    public static Vec3 getPlayerPos(ResourceLocation structure) {
        if (!TEMPLATES.containsKey(structure)) return TEMPLATES.get(DEFAULT).playerPos;
        return TEMPLATES.get(structure).playerPos;
    }

    public static Vec3 getBossPos(ResourceLocation structure) {
        if (!TEMPLATES.containsKey(structure)) return TEMPLATES.get(DEFAULT).bossPos;
        return TEMPLATES.get(structure).bossPos;
    }

    public static void clear() {
        TEMPLATES.clear();
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
