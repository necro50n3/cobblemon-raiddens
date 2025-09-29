package com.necro.raid.dens.common.blocks.block;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.RaidJoinEvent;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.*;
import com.necro.raid.dens.common.util.RaidDenRegistry;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RaidCrystalBlock extends BaseEntityBlock {
    public static final EnumProperty<RaidType> RAID_TYPE = EnumProperty.create("raid_type", RaidType.class);
    public static final EnumProperty<RaidTier> RAID_TIER = EnumProperty.create("raid_tier", RaidTier.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("is_active");
    public static final BooleanProperty CAN_RESET = BooleanProperty.create("can_reset");
    public static final EnumProperty<RaidCycleMode> CYCLE_MODE = EnumProperty.create("cycle_mode", RaidCycleMode.class);

    public RaidCrystalBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(ACTIVE, false)
            .setValue(RAID_TYPE, RaidType.NONE)
            .setValue(RAID_TIER, RaidTier.TIER_ONE)
            .setValue(CAN_RESET, false)
            .setValue(CYCLE_MODE, RaidCycleMode.NONE)
        );
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Boolean success = this.startOrJoinRaid(player, blockState, (RaidCrystalBlockEntity) level.getBlockEntity(blockPos), null);
        if (success == null || success) return InteractionResult.SUCCESS;
        else return InteractionResult.FAIL;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (CobblemonRaidDens.CONFIG.requires_key && !RaidUtils.isRaidDenKey(itemStack)) return ItemInteractionResult.FAIL;
        else if (level.isClientSide()) return ItemInteractionResult.SUCCESS;
        else if (!CobblemonRaidDens.CONFIG.requires_key) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        Boolean success = this.startOrJoinRaid(player, blockState, (RaidCrystalBlockEntity) level.getBlockEntity(blockPos), itemStack);
        if (success == null) return ItemInteractionResult.SUCCESS;
        else if (success) itemStack.consume(1, player);
        else player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.no_key"));
        return success ? ItemInteractionResult.CONSUME : ItemInteractionResult.FAIL;
    }

    private Boolean startOrJoinRaid(Player player, BlockState blockState, RaidCrystalBlockEntity blockEntity, @Nullable ItemStack key) {
        if (blockEntity.isBusy() || player.getServer() == null) return false;
        else if (!blockEntity.isActive(blockState) || blockEntity.isAtMaxClears()) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.is_not_active"));
            return false;
        }
        else if (RaidHelper.hasClearedRaid(blockEntity.getUuid(), player)) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.player_cleared"));
            return false;
        }
        else if (blockEntity.hasDimension() && blockEntity.isPlayerParticipating(player)) {
            Vec3 playerPos = RaidDenRegistry.getPlayerPos(blockEntity.getRaidStructure());
            RaidUtils.teleportPlayerToRaid((ServerPlayer) player, blockEntity.getDimension(), playerPos);
            return null;
        }
        else if (RaidHelper.isAlreadyHosting(player)) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.already_hosting"));
            return false;
        }
        else if (RaidHelper.isAlreadyParticipating(player)) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.already_participating"));
            return false;
        }
        else if (blockEntity.canSetRaidHost()) {
            boolean success = this.startRaid(player, blockEntity);
            if (!success) blockEntity.clearRaidHost();
            return success;
        }
        else if (blockEntity.isFull()) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.lobby_is_full"));
            return false;
        }
        else if (RaidHelper.isInQueue(player)) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.already_in_queue"));
            return false;
        }
        return this.requestJoinRaid(player, blockEntity, key);
    }

    private boolean requestJoinRaid(Player player, RaidCrystalBlockEntity blockEntity, @Nullable ItemStack key) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        ServerPlayer raidHost = server.getPlayerList().getPlayer(blockEntity.getRaidHost());
        if (raidHost == null) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.no_host"));
            return false;
        }

        RaidHelper.addToQueue(player, key);
        RaidHelper.addRequest(raidHost, player, blockEntity);
        RaidDenNetworkMessages.REQUEST_PACKET.accept(raidHost, player.getName().getString());
        return true;
    }

    private boolean startRaid(Player player, RaidCrystalBlockEntity blockEntity) {
        if (blockEntity.getLevel() == null || player.getServer() == null) return false;
        ResourceKey<Level> resourceKey = ModDimensions.createLevelKey(player.getStringUUID());
        if (DimensionHelper.isLevelRemovedOrPending(resourceKey)) {
            player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.pending_removal").withStyle(ChatFormatting.RED));
            return false;
        }

        blockEntity.setRaidHost(player);

        boolean success = RaidEvents.RAID_JOIN.postWithResult(new RaidJoinEvent((ServerPlayer) player, true, blockEntity.getRaidBoss()));
        if (!success) return false;

        ServerLevel level;
        try { level = this.getOrCreateDimension(blockEntity); }
        catch (Exception e) {
            player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.already_hosting").withStyle(ChatFormatting.RED));
            return false;
        }

        blockEntity.setDimension(level);
        if (!blockEntity.spawnRaidBoss()) {
            blockEntity.setQueueClose();
            player.sendSystemMessage(Component.translatable("message.cobblemonraiddens.raid.boss_spawn_failed").withStyle(ChatFormatting.RED));
            return false;
        }

        RaidHelper.addHost(player);
        blockEntity.addChunkTicket();
        blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos()).setUnsaved(true);

        Vec3 playerPos = RaidDenRegistry.getPlayerPos(blockEntity.getRaidStructure());
        RaidUtils.teleportPlayerToRaid((ServerPlayer) player, level, playerPos);
        return true;
    }

    private ServerLevel getOrCreateDimension(RaidCrystalBlockEntity blockEntity) {
        return blockEntity.getDimension() != null ? blockEntity.getDimension() : this.createDimension(blockEntity);
    }

    protected abstract ServerLevel createDimension(RaidCrystalBlockEntity blockEntity);

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!level.isClientSide() && level.getBlockEntity(blockPos) instanceof RaidCrystalBlockEntity blockEntity) {
            blockEntity.closeRaid(blockPos);
            RaidHelper.resetClearedRaids(blockEntity.getUuid());
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RAID_TYPE);
        builder.add(RAID_TIER);
        builder.add(ACTIVE);
        builder.add(CAN_RESET);
        builder.add(CYCLE_MODE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}
