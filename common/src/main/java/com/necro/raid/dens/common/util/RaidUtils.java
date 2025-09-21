package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.items.ItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashSet;
import java.util.Set;

public class RaidUtils {
    private static final Set<String> MOVE_BLACKLIST = new HashSet<>();

    public static boolean isMoveBlacklisted(String move) {
        return MOVE_BLACKLIST.contains(move);
    }

    public static boolean hasSkyAccess(LevelReader level, BlockPos blockPos) {
        int topY = level.getChunk(blockPos).getHeight(Heightmap.Types.MOTION_BLOCKING, blockPos.getX() & 15, blockPos.getZ() & 15);
        return blockPos.getY() >= topY;
    }

    public static void teleportPlayerSafe(Player player, ServerLevel level, BlockPos targetPos, float yaw, float pitch) {
        int groundY = level.getChunk(targetPos).getHeight(Heightmap.Types.MOTION_BLOCKING, targetPos.getX(), targetPos.getZ());
        BlockPos groundPos = targetPos.atY((int) Mth.absMax(groundY, targetPos.getY()));

        if (RaidUtils.isSafe(level, groundPos.north()) && level.getBlockState(groundPos.north().below()).isSolidRender(level, groundPos.north().below())) {
            player.teleportTo(level, groundPos.getX() + 0.5, groundPos.getY(), groundPos.getZ() - 0.5,
                new HashSet<>(), yaw, pitch);
            return;
        }

        int radius = 1;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos offset = targetPos.offset(dx, 0, dz);
                int topY = level.getChunk(offset).getHeight(Heightmap.Types.MOTION_BLOCKING, offset.getX(), offset.getZ());
                offset = offset.atY((int) Mth.absMax(topY, targetPos.getY()));
                if (RaidUtils.isSafe(level, offset) && level.getBlockState(offset.below()).isSolidRender(level, offset.below())) {
                    player.teleportTo(level,offset.getX() + 0.5, offset.getY(), offset.getZ() + 0.5,
                        new HashSet<>(), yaw, pitch);
                    return;
                }
            }
        }

        player.teleportTo(level, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() - 0.5,
            new HashSet<>(), yaw, pitch);
    }

    private static boolean isSafe(ServerLevel world, BlockPos pos) {
        BlockState block = world.getBlockState(pos);
        BlockState above = world.getBlockState(pos.above());

        return !block.is(Blocks.LAVA) &&
            block.getCollisionShape(world, pos).isEmpty() &&
            above.getCollisionShape(world, pos.above()).isEmpty();
    }

    public static boolean isRaidDenKey(ItemStack itemStack) {
        return itemStack.is(ItemTags.RAID_DEN_KEY) || itemStack.getOrDefault(ModComponents.RAID_DEN_KEY.value(), false);
    }

    public static boolean isCustomDimension(Level level) {
        return level.dimensionTypeRegistration().is(ModDimensions.RAIDDIM_TYPE);
    }

    public static boolean cannotBreakOrPlace(Player player, Level level) {
        return RaidUtils.isCustomDimension(level) && !player.isCreative();
    }

    public static boolean cannotBreakOrPlace(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        return RaidUtils.isCustomDimension(level) && !player.isCreative() && !(level.getBlockEntity(hitResult.getBlockPos()) instanceof RaidHomeBlockEntity);
    }

    public static boolean canBreakOrPlace(Level level, Player player, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        return !RaidUtils.cannotBreakOrPlace(player, level);
    }

    public static InteractionResult canBreakOrPlace(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        return RaidUtils.cannotBreakOrPlace(player, level, hand, hitResult) ? InteractionResult.FAIL : InteractionResult.PASS;
    }

    public static void init() {
        MOVE_BLACKLIST.addAll(CobblemonRaidDens.MOVE_CONFIG.blacklist);
    }
}
