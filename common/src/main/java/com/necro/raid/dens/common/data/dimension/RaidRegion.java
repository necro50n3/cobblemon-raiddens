package com.necro.raid.dens.common.data.dimension;

import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.registry.RaidDenRegistry;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RaidRegion {
    private final BlockPos centre;
    private final AABB region;
    private ResourceLocation structure;
    private final BlockPos homePos;
    private final ResourceLocation homeLevel;

    public RaidRegion(BlockPos centre, ResourceLocation structure, BlockPos homePos, ResourceLocation homeLevel) {
        this.centre = centre;
        this.region = new AABB(
            this.centre.getX() - 1000, -64, this.centre.getZ() - 1000,
            this.centre.getX() + 1000, 64, this.centre.getZ() + 1000
        );
        this.structure = structure;
        this.homePos = homePos;
        this.homeLevel = homeLevel;
    }

    public boolean contains(Vec3 position) {
        return this.region.contains(position);
    }

    public BlockPos getOffset() {
        return BlockPos.containing(RaidDenRegistry.getOffset(this.structure).add(this.centre.getBottomCenter()));
    }

    public Vec3 getPlayerPos() {
        return RaidDenRegistry.getPlayerPos(this.structure).add(this.centre.getBottomCenter());
    }

    public Vec3 getBossPos() {
        return RaidDenRegistry.getBossPos(this.structure).add(this.centre.getBottomCenter());
    }

    public void returnHome(ServerPlayer player, MinecraftServer server) {
        ServerLevel home = server.getLevel(ResourceKey.create(Registries.DIMENSION, this.homeLevel));
        if (home == null) home = server.overworld();
        RaidUtils.teleportPlayerSafe(player, home, this.homePos, player.getYHeadRot(), player.getXRot());
    }

    public void clearRegion(ServerLevel level) {
        for (Entity e : level.getAllEntities()) {
            if (e != null) e.discard();
        }

        int minX = this.centre.getX() - 100;
        int minY = -64;
        int minZ = this.centre.getZ() - 100;
        int maxX = this.centre.getX() + 100;
        int maxY = 64;
        int maxZ = this.centre.getZ() + 100;

        int chunkMinX = minX >> 4;
        int chunkMaxX = maxX >> 4;
        int chunkMinZ = minZ >> 4;
        int chunkMaxZ = maxZ >> 4;

        for (int cx = chunkMinX; cx <= chunkMaxX; cx++) {
            for (int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);

                int sectionMinY = minY >> 4;
                int sectionMaxY = maxY >> 4;

                for (int sy = sectionMinY; sy <= sectionMaxY; sy++) {
                    LevelChunkSection section = chunk.getSection(sy);

                    for (int x = 0; x < 16; x++) {
                        int worldX = (cx << 4) + x;
                        if (worldX < minX || worldX > maxX) continue;

                        for (int y = 0; y < 16; y++) {
                            int worldY = (sy << 4) + y;
                            if (worldY < minY || worldY > maxY) continue;

                            for (int z = 0; z < 16; z++) {
                                int worldZ = (cz << 4) + z;
                                if (worldZ < minZ || worldZ > maxZ) continue;

                                section.setBlockState(x, y, z, Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                    section.recalcBlockCounts();
                }
                chunk.setUnsaved(true);
            }
        }
    }

    public void placeStructure(ServerLevel level) {
        StructureTemplateManager manager = level.getServer().getStructureManager();
        StructureTemplate template = manager.get(this.structure).orElseGet(() -> {
            this.structure = RaidDenRegistry.DEFAULT;
            return manager.getOrCreate(this.structure);
        });
        StructurePlaceSettings settings = new StructurePlaceSettings();
        settings.clearProcessors();
        settings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);

        BlockPos offset = this.getOffset();

        template.placeInWorld(level, offset, offset, settings, level.getRandom(), 2);
        level.setBlockAndUpdate(BlockPos.ZERO, ModBlocks.INSTANCE.getRaidHomeBlock().defaultBlockState());
    }

    public static RaidRegion load(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag centreTag = tag.getCompound("centre");
        BlockPos centre = new BlockPos(
            centreTag.getInt("centre_x"),
            centreTag.getInt("centre_y"),
            centreTag.getInt("centre_z")
        );

        ResourceLocation structure = ResourceLocation.parse(tag.getString("structure"));

        CompoundTag homePosTag = tag.getCompound("home_pos");
        BlockPos homePos = new BlockPos(
            homePosTag.getInt("home_x"),
            homePosTag.getInt("home_y"),
            homePosTag.getInt("home_z")
        );

        ResourceLocation homeLevel = ResourceLocation.parse(tag.getString("home_level"));

        return new RaidRegion(centre, structure, homePos, homeLevel);
    }

    public CompoundTag save(CompoundTag tag) {
        CompoundTag centre = new CompoundTag();
        centre.putInt("centre_x", this.centre.getX());
        centre.putInt("centre_y", this.centre.getY());
        centre.putInt("centre_z", this.centre.getZ());
        tag.put("centre", centre);

        tag.putString("structure", this.structure.toString());

        CompoundTag homePos = new CompoundTag();
        centre.putInt("home_x", this.homePos.getX());
        centre.putInt("home_y", this.homePos.getY());
        centre.putInt("home_z", this.homePos.getZ());
        tag.put("home_pos", homePos);

        tag.putString("home_level", this.homeLevel.toString());

        return tag;
    }
}
