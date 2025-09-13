package com.necro.raid.dens.common.blocks.block;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.RaidJoinEvent;
import com.necro.raid.dens.common.items.ItemTags;
import com.necro.raid.dens.common.raids.RaidCycleMode;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

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
        return this.startOrJoinRaid(player, blockState, (RaidCrystalBlockEntity) level.getBlockEntity(blockPos), level, blockPos, null) ?
            InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (CobblemonRaidDens.CONFIG.requires_key && !RaidUtils.isRaidDenKey(itemStack)) return ItemInteractionResult.FAIL;
        else if (level.isClientSide()) return ItemInteractionResult.SUCCESS;
        else if (!CobblemonRaidDens.CONFIG.requires_key) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        return this.startOrJoinRaid(player, blockState, (RaidCrystalBlockEntity) level.getBlockEntity(blockPos), level, blockPos, itemStack) ?
            ItemInteractionResult.CONSUME : ItemInteractionResult.FAIL;
    }

    private boolean startOrJoinRaid(Player player, BlockState blockState, RaidCrystalBlockEntity blockEntity, Level level, BlockPos blockPos, @Nullable ItemStack key) {
        if (blockEntity.isBusy()) return false;
        else if (!blockEntity.isActive(blockState) || blockEntity.isAtMaxClears()) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.is_not_active"));
            return false;
        }
        else if (RaidHelper.hasClearedRaid(level, blockPos, player)) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.player_cleared"));
            return false;
        }
        else if (blockEntity.hasDimension() && blockEntity.isPlayerParticipating(player)) {
            player.teleportTo(blockEntity.getDimension(), 0.5, 0, -0.5, new HashSet<>(), 180f, 0f);
            return true;
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
        else if (!RaidHelper.addToQueue(player, key)) {
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.already_in_queue"));
            return false;
        }
        this.requestJoinRaid(player, blockEntity, blockPos);
        return true;
    }

    private void requestJoinRaid(Player player, RaidCrystalBlockEntity blockEntity, BlockPos blockPos) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        Player raidHost = server.getPlayerList().getPlayer(blockEntity.getRaidHost());
        if (raidHost == null) return;
        Component component = Component.translatable("message.cobblemonraiddens.raid.request_to_join", player.getName())
            .append(Component.translatable("message.cobblemonraiddens.raid.request_accept")
                .withStyle(Style.EMPTY.applyFormat(ChatFormatting.GREEN).applyFormat(ChatFormatting.BOLD).withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, RaidHelper.acceptRaidCommand(raidHost, player, blockEntity, blockPos))
                )))
            .append(Component.translatable("message.cobblemonraiddens.raid.request_deny")
                .withStyle(Style.EMPTY.applyFormat(ChatFormatting.RED).applyFormat(ChatFormatting.BOLD).withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, RaidHelper.rejectRaidCommand(raidHost, player))
                )));
        raidHost.sendSystemMessage(component);
    }

    private boolean startRaid(Player player, RaidCrystalBlockEntity blockEntity) {
        blockEntity.setRaidHost(player);

        ServerLevel level;
        try { level = this.getOrCreateDimension(blockEntity); }
        catch (IllegalStateException e) {
            player.sendSystemMessage(Component.translatable("error.cobblemonraiddens.dimension_exist").withStyle(ChatFormatting.RED));
            return false;
        }

        boolean success = RaidEvents.RAID_JOIN.postWithResult(new RaidJoinEvent((ServerPlayer) player, true, blockEntity.getRaidBoss()));
        if (!success) return false;

        RaidHelper.addHost(player);

        blockEntity.setDimension(level);
        blockEntity.spawnRaidBoss();
        blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos()).setUnsaved(true);
        player.teleportTo(level, 0.5, 0, -0.5, new HashSet<>(), 180f, 0f);
        return true;
    }

    private ServerLevel getOrCreateDimension(RaidCrystalBlockEntity blockEntity) {
        return blockEntity.getDimension() != null ? blockEntity.getDimension() : this.createDimension(blockEntity);
    }

    protected abstract ServerLevel createDimension(RaidCrystalBlockEntity blockEntity);

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!level.isClientSide() && level.getBlockEntity(blockPos) instanceof RaidCrystalBlockEntity raidCrystalBlockEntity) {
            raidCrystalBlockEntity.closeRaid(blockPos);
            RaidHelper.resetClearedRaids(level, blockPos);
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
