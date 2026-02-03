package com.necro.raid.dens.common.mixins.den;

import com.necro.raid.dens.common.util.IRaidDenTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin implements IRaidDenTemplate {
    @Shadow
    @Final
    private List<StructureTemplate.Palette> palettes;

    @Shadow
    @Final
    private List<StructureTemplate.StructureEntityInfo> entityInfoList;

    @Shadow
    private Vec3i size;

    @Unique
    private boolean crd_raidPlacerEnabled;

    @Shadow
    public static List<StructureTemplate.StructureBlockInfo> processBlockInfos(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, List<StructureTemplate.StructureBlockInfo> list) {
        return null;
    }

    @Shadow
    protected abstract void placeEntities(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, Mirror mirror, Rotation rotation, BlockPos blockPos2, @Nullable BoundingBox boundingBox, boolean bl);

    @Override
    public void crd_setRaidPlacer(boolean enable) {
        this.crd_raidPlacerEnabled = enable;
    }

    @Inject(method = "placeInWorld", at = @At("HEAD"), cancellable = true)
    private void placeInWorldInject(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int i, CallbackInfoReturnable<Boolean> cir) {
        if (this.palettes.isEmpty()) return;
        else if (!this.crd_raidPlacerEnabled) return;

        List<StructureTemplate.StructureBlockInfo> list = structurePlaceSettings.getRandomPalette(this.palettes, blockPos).blocks();
        if ((!list.isEmpty() || !structurePlaceSettings.isIgnoreEntities() && !this.entityInfoList.isEmpty()) && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
            BoundingBox boundingBox = structurePlaceSettings.getBoundingBox();

            List<StructureTemplate.StructureBlockInfo> infos = processBlockInfos(serverLevelAccessor, blockPos, blockPos2, structurePlaceSettings, list);
            assert infos != null;

            for(StructureTemplate.StructureBlockInfo structureBlockInfo : infos) {
                BlockPos blockPos3 = structureBlockInfo.pos();
                if (boundingBox == null || boundingBox.isInside(blockPos3)) {
                    BlockState blockState = structureBlockInfo.state().mirror(structurePlaceSettings.getMirror()).rotate(structurePlaceSettings.getRotation());
                    if (structureBlockInfo.nbt() != null) {
                        BlockEntity blockEntity = serverLevelAccessor.getBlockEntity(blockPos3);
                        Clearable.tryClear(blockEntity);
                        serverLevelAccessor.setBlock(blockPos3, Blocks.BARRIER.defaultBlockState(), i, 0);
                    }

                    if (serverLevelAccessor.setBlock(blockPos3, blockState, i, 0)) {
                        if (structureBlockInfo.nbt() != null) {
                            BlockEntity blockEntity = serverLevelAccessor.getBlockEntity(blockPos3);
                            if (blockEntity != null) {
                                if (blockEntity instanceof RandomizableContainer) {
                                    structureBlockInfo.nbt().putLong("LootTableSeed", randomSource.nextLong());
                                }

                                blockEntity.loadWithComponents(structureBlockInfo.nbt(), serverLevelAccessor.registryAccess());
                            }
                        }
                    }
                }
            }

            if (!structurePlaceSettings.isIgnoreEntities()) {
                this.placeEntities(serverLevelAccessor, blockPos, structurePlaceSettings.getMirror(), structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot(), boundingBox, structurePlaceSettings.shouldFinalizeEntities());
            }

            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }
}
