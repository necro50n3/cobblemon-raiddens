package com.necro.raid.dens.common.blocks.block;

import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public abstract class RaidHomeBlock extends BaseEntityBlock {
    public RaidHomeBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            RaidHomeBlockEntity blockEntity = (RaidHomeBlockEntity) level.getBlockEntity(blockPos);
            if (blockEntity == null) return InteractionResult.FAIL;
            BlockPos homePos = blockEntity.getHomePos();
            if (homePos == null) return InteractionResult.FAIL;
            ServerLevel home = level.getServer().getLevel(blockEntity.getHome());
            if (home == null) return InteractionResult.FAIL;

            PlayerExtensionsKt.party((ServerPlayer) player).forEach(pokemon -> {
                if (pokemon.getState() instanceof ActivePokemonState) pokemon.recall();
            });

            RaidUtils.teleportPlayerSafe(player, home, homePos, player.getYHeadRot(), player.getXRot());

            if (level.getEntitiesOfClass(LivingEntity.class, new AABB(blockPos).inflate(32)).isEmpty()) {
                if (home.getBlockEntity(homePos) instanceof RaidCrystalBlockEntity raidCrystalBlockEntity) {
                    raidCrystalBlockEntity.clearRaid();
                }
            }
            else if (level.getEntitiesOfClass(Player.class, new AABB(blockPos).inflate(32)).isEmpty()) {
                if (home.getBlockEntity(homePos) instanceof RaidCrystalBlockEntity raidCrystalBlockEntity) {
                    raidCrystalBlockEntity.setQueueClose();
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
