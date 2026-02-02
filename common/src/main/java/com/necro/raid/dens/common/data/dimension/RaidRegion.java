package com.necro.raid.dens.common.data.dimension;

import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.registry.RaidDenRegistry;
import com.necro.raid.dens.common.util.IRaidTeleporter;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RaidRegion {
    private static final int RADIUS = 128;

    private final BlockPos centre;
    private final AABB bound;
    private ResourceLocation structure;

    public RaidRegion(BlockPos centre, ResourceLocation structure) {
        this.centre = centre;
        this.bound = new AABB(centre.getX() - RADIUS, -64, centre.getZ() - RADIUS, centre.getX() + RADIUS, 128, centre.getZ() + RADIUS);
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
        if (!RaidUtils.isRaidDimension(level)) return;

        int minX = this.centre.getX() - RADIUS;
        int maxX = this.centre.getX() + RADIUS;
        int minZ = this.centre.getZ() - RADIUS;
        int maxZ = this.centre.getZ() + RADIUS;

        int chunkMinX = minX >> 4;
        int chunkMaxX = maxX >> 4;
        int chunkMinZ = minZ >> 4;
        int chunkMaxZ = maxZ >> 4;

        level.getEntitiesOfClass(ServerPlayer.class, this.bound())
            .forEach(player -> {
                RaidUtils.leaveRaid(player);
                ((IRaidTeleporter) player).crd_returnHome();
            });

        for (Entity e : level.getEntitiesOfClass(Entity.class, this.bound())) {
            if (e != null && !(e instanceof Player)) e.discard();
        }

        for (int cx = chunkMinX; cx <= chunkMaxX; cx++) {
            for (int cz = chunkMinZ; cz <= chunkMaxZ; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);

                LevelChunkSection[] sections = chunk.getSections();
                for (int i = 0; i < sections.length; i++) {
                    LevelChunkSection section = sections[i];
                    if (section == null) continue;

                    if (!section.hasOnlyAir()) {
                        chunk.getSections()[i] = new LevelChunkSection(level.registryAccess().registryOrThrow(Registries.BIOME));
                        chunk.getBlockEntities().keySet().forEach(chunk::removeBlockEntity);
                        section.recalcBlockCounts();
                    }
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
        template.placeInWorld(level, offset, offset, settings, level.getRandom(), 0);

        level.setBlockAndUpdate(this.centre(), ModBlocks.INSTANCE.getRaidHomeBlock().defaultBlockState());

        ChunkPos chunkPos = new ChunkPos(this.centre());
        level.getChunkSource().addRegionTicket(TicketType.FORCED, chunkPos, 1, chunkPos);
    }

    public void removeRegionTicket(ServerLevel level) {
        ChunkPos chunkPos = new ChunkPos(this.centre());
        level.getChunkSource().removeRegionTicket(TicketType.FORCED, chunkPos, 1, chunkPos);
    }
}
