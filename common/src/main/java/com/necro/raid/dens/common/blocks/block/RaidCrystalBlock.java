package com.necro.raid.dens.common.blocks.block;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import com.necro.raid.dens.common.data.raid.*;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.RaidJoinEvent;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.raids.helpers.RaidRegionHelper;
import com.necro.raid.dens.common.registry.RaidRegistry;
import com.necro.raid.dens.common.util.ComponentUtils;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public abstract class RaidCrystalBlock extends BaseEntityBlock {
    public static final EnumProperty<RaidType> RAID_TYPE = EnumProperty.create("raid_type", RaidType.class);
    public static final EnumProperty<RaidTier> RAID_TIER = EnumProperty.create("raid_tier", RaidTier.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("is_active");
    public static final BooleanProperty CAN_RESET = BooleanProperty.create("can_reset");
    public static final EnumProperty<RaidCycleMode> CYCLE_MODE = EnumProperty.create("cycle_mode", RaidCycleMode.class);
    public static final BooleanProperty IS_NATURAL = BooleanProperty.create("is_natural");

    private static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 0.9375, 1);

    public RaidCrystalBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(ACTIVE, true)
            .setValue(RAID_TYPE, RaidType.STELLAR)
            .setValue(RAID_TIER, RaidTier.TIER_ONE)
            .setValue(CAN_RESET, true)
            .setValue(CYCLE_MODE, RaidCycleMode.CONFIG)
            .setValue(IS_NATURAL, true)
        );
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockState, Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull BlockHitResult blockHitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(blockPos) instanceof RaidCrystalBlockEntity raidCrystal)) return InteractionResult.FAIL;
        boolean success = this.startOrJoinRaid(player, blockState, raidCrystal, null);
        return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack itemStack, @NotNull BlockState blockState, Level level, @NotNull BlockPos blockPos, @NotNull Player player, @NotNull InteractionHand interactionHand, @NotNull BlockHitResult blockHitResult) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return ItemInteractionResult.FAIL;
        if (raidCrystal.getRaidBoss() == null) return ItemInteractionResult.FAIL;
        else if (RaidRegionHelper.getRegion(raidCrystal.getUuid()) != null && raidCrystal.isPlayerParticipating(player)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (raidCrystal.getRaidBoss().getKey().isEmpty() && !CobblemonRaidDens.TIER_CONFIG.get(blockState.getValue(RAID_TIER)).requiresKey()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        else if (!this.handleKey(player, raidCrystal, itemStack)) return ItemInteractionResult.FAIL;

        boolean success = this.startOrJoinRaid(player, blockState, raidCrystal, itemStack);
        if (success) itemStack.consume(1, player);
        return success ? ItemInteractionResult.CONSUME : ItemInteractionResult.FAIL;
    }

    private boolean startOrJoinRaid(Player player, BlockState blockState, RaidCrystalBlockEntity blockEntity, @Nullable ItemStack key) {
        if (player.getServer() == null) return false;
        else if (!blockEntity.isActive(blockState) || blockEntity.isAtMaxClears()) {
            player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.is_not_active"), true);
            return false;
        }
        else if (RaidHelper.hasClearedRaid(blockEntity.getUuid(), player)) {
            player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.player_cleared"), true);
            return false;
        }
        RaidRegion region = RaidRegionHelper.getRegion(blockEntity.getUuid());
        if (region != null && blockEntity.isPlayerParticipating(player)) {
            RaidDenNetworkMessages.JOIN_RAID.accept((ServerPlayer) player, true);
            RaidUtils.teleportPlayerToRaid((ServerPlayer) player, player.getServer(), region);
            blockEntity.syncAspects((ServerPlayer) player);
            return true;
        }
        else if (RaidJoinHelper.isParticipatingOrInQueue(player, true)) {
            // System message is handled by checker
            return false;
        }
        else if (blockEntity.canSetRaidHost()) {
            boolean success = this.startRaid(player, blockEntity);
            if (!success) blockEntity.clearRaidHost();
            return success;
        }
        else if (blockEntity.isFull()) {
            player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.lobby_is_full"), true);
            return false;
        }
        return this.requestJoinRaid(player, blockEntity, key);
    }

    private boolean requestJoinRaid(Player player, RaidCrystalBlockEntity blockEntity, @Nullable ItemStack key) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        ServerPlayer raidHost = server.getPlayerList().getPlayer(blockEntity.getRaidHost());
        if (raidHost == null) {
            player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.no_host"), true);
            return false;
        }

        RaidJoinHelper.addToQueue(player, key);
        RaidHelper.addRequest(raidHost, player);
        RaidDenNetworkMessages.REQUEST_PACKET.accept(raidHost, player.getName().getString());
        return true;
    }

    private boolean startRaid(Player player, RaidCrystalBlockEntity blockEntity) {
        if (player.getServer() == null) return false;

        blockEntity.setRaidHost(player);

        boolean success = RaidEvents.RAID_JOIN.postWithResult(new RaidJoinEvent((ServerPlayer) player, true, blockEntity.getRaidBoss()));
        if (!success) return false;

        ResourceLocation structure = blockEntity.getRaidBoss().getRandomDen(player.level().getRandom());
        RaidRegion region = RaidRegionHelper.createRegion(blockEntity.getUuid(), structure);
        if (region == null || !blockEntity.spawnRaidBoss()) {
            blockEntity.closeRaid();
            player.displayClientMessage(ComponentUtils.getErrorMessage("message.cobblemonraiddens.raid.boss_spawn_failed"), true);
            return false;
        }

        if (!RaidJoinHelper.addParticipant(player, blockEntity.getUuid(), true, true)) return false;
        RaidHelper.initRequest((ServerPlayer) player, blockEntity);

        RaidInstance raid = RaidHelper.ACTIVE_RAIDS.get(blockEntity.getUuid());
        raid.addPlayer((ServerPlayer) player);
        RaidUtils.teleportPlayerToRaid((ServerPlayer) player, player.getServer(), region);
        blockEntity.syncAspects((ServerPlayer) player);
        player.displayClientMessage(ComponentUtils.getSystemMessage(Component.translatable("message.cobblemonraiddens.raid.raid_start", raid.getBossEntity().getDisplayName())), true);
        return true;
    }

    private boolean handleKey(Player player, RaidCrystalBlockEntity blockEntity, ItemStack itemStack) {
        RaidBoss boss = blockEntity.getRaidBoss();
        UniqueKey key = boss.getKey();
        if (!key.isEmpty()) {
            if (blockEntity.isOpen()) return true;
            else if (!key.matches(itemStack)) {
                player.displayClientMessage(ComponentUtils.getSystemMessage(Component.translatable("message.cobblemonraiddens.raid.no_unique_key", key.item().split(":")[1])), true);
                return false;
            }
            else if (!CobblemonRaidDens.TIER_CONFIG.get(boss.getTier()).allRequireUniqueKey()) blockEntity.setOpen();
        }
        else if (CobblemonRaidDens.TIER_CONFIG.get(boss.getTier()).requiresKey() && !RaidUtils.isRaidDenKey(itemStack)) {
            player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.no_key"), true);
            return false;
        }
        return true;
    }

    @Override
    protected void onRemove(@NotNull BlockState blockState, Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState2, boolean bl) {
        if (!level.isClientSide() && level.getBlockEntity(blockPos) instanceof RaidCrystalBlockEntity blockEntity) {
            blockEntity.closeRaid();
            RaidHelper.resetClearedRaids(blockEntity.getUuid());
            RaidInstance raid = RaidHelper.ACTIVE_RAIDS.get(blockEntity.getUuid());
            if (raid != null) raid.closeRaid(level.getServer(), true);
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState blockState = this.defaultBlockState();
        if (context.getPlayer() != null && !context.getPlayer().hasInfiniteMaterials()) blockState = blockState.setValue(IS_NATURAL, false);

        ItemStack itemStack = context.getItemInHand();
        RaidBoss boss = RaidRegistry.getRaidBoss(itemStack.get(ModComponents.BOSS_COMPONENT.value()));
        if (boss != null) {
            blockState = blockState.setValue(RAID_TYPE, boss.getType()).setValue(RAID_TIER, boss.getTier());

            int clears = itemStack.getOrDefault(ModComponents.RAID_CLEAR_COMPONENT.value(), 0);
            if (clears >= boss.getMaxClears()) blockState = blockState.setValue(ACTIVE, false);
        }

        return blockState;
    }

    @Override
    public void setPlacedBy(Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, LivingEntity livingEntity, @NotNull ItemStack itemStack) {
        if (level.isClientSide) return;
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return;

        String uuid = itemStack.get(ModComponents.UUID_COMPONENT.value());
        ResourceLocation bucket = itemStack.get(ModComponents.BUCKET_COMPONENT.value());
        ResourceLocation boss = itemStack.get(ModComponents.BOSS_COMPONENT.value());
        Integer clears = itemStack.get(ModComponents.RAID_CLEAR_COMPONENT.value());
        Long lastReset = itemStack.get(ModComponents.LAST_RESET_COMPONENT.value());
        List<String> aspects = itemStack.get(ModComponents.ASPECTS_COMPONENT.value());

        if (uuid != null) raidCrystal.setUuid(UUID.fromString(uuid));
        if (bucket != null) raidCrystal.setRaidBucket(bucket);
        if (boss != null) raidCrystal.setRaidBoss(boss, lastReset == null ? 0 : lastReset);
        if (clears != null) raidCrystal.setClears(clears);
        if (aspects != null) raidCrystal.setAspects(new HashSet<>(aspects));
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState blockState, LootParams.@NotNull Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return List.of();
        else if (blockState.getValue(IS_NATURAL)) return List.of();

        ItemStack itemStack = new ItemStack(this);
        itemStack.set(ModComponents.UUID_COMPONENT.value(), raidCrystal.getUuid().toString());
        if (raidCrystal.getRaidBucketLocation() != null) itemStack.set(ModComponents.BUCKET_COMPONENT.value(), raidCrystal.getRaidBucketLocation());
        if (raidCrystal.getRaidBossLocation() != null) itemStack.set(ModComponents.BOSS_COMPONENT.value(), raidCrystal.getRaidBossLocation());
        itemStack.set(ModComponents.RAID_CLEAR_COMPONENT.value(), raidCrystal.getClears());
        itemStack.set(ModComponents.LAST_RESET_COMPONENT.value(), raidCrystal.getLastReset());
        if (raidCrystal.getAspects() != null) itemStack.set(ModComponents.ASPECTS_COMPONENT.value(), raidCrystal.getAspects().stream().toList());
        return List.of(itemStack);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RAID_TYPE);
        builder.add(RAID_TIER);
        builder.add(ACTIVE);
        builder.add(CAN_RESET);
        builder.add(CYCLE_MODE);
        builder.add(IS_NATURAL);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return null;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState blockState, @NotNull PathComputationType pathComputationType) {
        return false;
    }
}
