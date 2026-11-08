package com.necro.raid.dens.common.registry.custom;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.structure.RaidDenPool;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RaidDenPoolRegistry extends CustomRegistry<ResourceLocation, RaidDenPool> {
    private final Object2ObjectOpenHashMap<ResourceLocation, RaidStructureData> templates;

    public RaidDenPoolRegistry() {
        super(ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den/basic"));
        this.templates = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public void register(ResourceLocation id, RaidDenPool entry) {
        if (id == null) throw new IllegalArgumentException("Attempted to register a null key");
        if (this.registry.containsKey(id)) this.registry.get(id).addDens(entry);
        else this.registry.put(id, entry);
    }

    public void register(ResourceLocation id, CompoundTag structure) {
        this.templates.put(id, new RaidStructureData(structure));
    }

    public List<ResourceLocation> getStructures(ResourceLocation pool) {
        if (!this.registry.containsKey(pool)) return List.of();
        else return this.registry.get(pool).getDens();
    }

    public boolean isNotValidStructure(ResourceLocation structure) {
        return !this.templates.containsKey(structure);
    }

    public Vec3 getOffset(ResourceLocation structure) {
        if (!this.templates.containsKey(structure)) return this.templates.get(this.defaultKey).offset;
        return this.templates.get(structure).offset;
    }

    public Vec3 getPlayerPos(ResourceLocation structure) {
        if (!this.templates.containsKey(structure)) return this.templates.get(this.defaultKey).playerPos;
        return this.templates.get(structure).playerPos;
    }

    public Vec3 getBossPos(ResourceLocation structure) {
        if (!this.templates.containsKey(structure)) return this.templates.get(this.defaultKey).bossPos;
        return this.templates.get(structure).bossPos;
    }

    public double getScaleModifier(ResourceLocation structure) {
        if (!this.templates.containsKey(structure)) return 1.0;
        return this.templates.get(structure).scaleMod;
    }

    public ResourceLocation defaultKey() {
        return this.defaultKey;
    }

    @Override
    public void clear() {
        super.clear();
        this.templates.clear();
    }

    private static class RaidStructureData {
        private final Vec3 offset;
        private final Vec3 playerPos;
        private final Vec3 bossPos;
        private final double scaleMod;

        private RaidStructureData(CompoundTag tag) {
            this.offset = new Vec3(tag.getDouble("offset_x"), tag.getDouble("offset_y"), tag.getDouble("offset_z"));
            this.playerPos = new Vec3(tag.getDouble("player_x"), tag.getDouble("player_y"), tag.getDouble("player_z"));
            this.bossPos = new Vec3(tag.getDouble("boss_x"), tag.getDouble("boss_y"), tag.getDouble("boss_z"));

            double scale = Math.max(tag.getDouble("scale_modifier"), 0.0);
            this.scaleMod = scale == 0.0 ? 1.0 : scale;
        }
    }
}
