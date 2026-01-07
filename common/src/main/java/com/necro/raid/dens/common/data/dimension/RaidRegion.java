package com.necro.raid.dens.common.data.dimension;

import com.necro.raid.dens.common.blocks.BlockTags;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.registry.RaidDenRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RaidRegion {
    private final BlockPos centre;
    private final AABB bound;
    private ResourceLocation structure;

    public RaidRegion(BlockPos centre, ResourceLocation structure) {
        this.centre = centre;
        this.bound = new AABB(centre.getX() - 128, -64, centre.getZ() - 128, centre.getX() + 128, 128, centre.getZ() + 128);
        this.structure = structure;
    }

    public BlockPos centre() {
        return this.centre;
    }

    public AABB bound() {
        return this.bound;
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

    public void clearRegion(ServerLevel level) {
        int radius = 128;
        int minX = this.centre.getX() - radius;
        int maxX = this.centre.getX() + radius;
        int minY = -64;
        int maxY = 64;
        int minZ = this.centre.getZ() - radius;
        int maxZ = this.centre.getZ() + radius;

        int chunkMinX = minX >> 4;
        int chunkMaxX = maxX >> 4;
        int chunkMinZ = minZ >> 4;
        int chunkMaxZ = maxZ >> 4;

        for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(minX, minY, minZ, maxX, maxY, maxZ))) {
            if (e != null && !(e instanceof Player)) e.discard();
        }

        for (int cx = chunkMinX; cx <= chunkMaxX; cx++) {
            for (int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);

                LevelChunkSection[] sections = chunk.getSections();
                int chunkMinSection = chunk.getMinSection();

                for (int i = 0; i < sections.length; i++) {
                    LevelChunkSection section = sections[i];
                    if (section == null) continue;

                    int sy = i + chunkMinSection;
                    int sectionStartY = sy << 4;

                    int localMinY = Math.max(0, minY - sectionStartY);
                    int localMaxY = Math.min(15, maxY - sectionStartY);

                    for (int x = 0; x < 16; x++) {
                        int worldX = (cx << 4) + x;
                        if (worldX < minX || worldX > maxX) continue;

                        for (int y = localMinY; y <= localMaxY; y++) {
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
        settings.addProcessor(new ProtectedBlockProcessor(BlockTags.RAID_CRYSTAL));

        BlockPos offset = this.getOffset();
        template.placeInWorld(level, offset, offset, settings, level.getRandom(), 2);

        level.setBlockAndUpdate(this.centre(), ModBlocks.INSTANCE.getRaidHomeBlock().defaultBlockState());
    }

    public static RaidRegion load(CompoundTag tag) {
        CompoundTag centreTag = tag.getCompound("centre");
        BlockPos centre = new BlockPos(
            centreTag.getInt("centre_x"),
            centreTag.getInt("centre_y"),
            centreTag.getInt("centre_z")
        );

        ResourceLocation structure = ResourceLocation.parse(tag.getString("structure"));

        return new RaidRegion(centre, structure);
    }

    public CompoundTag save(CompoundTag tag) {
        CompoundTag centre = new CompoundTag();
        centre.putInt("centre_x", this.centre.getX());
        centre.putInt("centre_y", this.centre.getY());
        centre.putInt("centre_z", this.centre.getZ());
        tag.put("centre", centre);

        tag.putString("structure", this.structure.toString());

        return tag;
    }
}
