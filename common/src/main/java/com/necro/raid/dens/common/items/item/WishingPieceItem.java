package com.necro.raid.dens.common.items.item;

import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class WishingPieceItem extends Item {
    public WishingPieceItem() {
        super(new Properties().component(ModComponents.MIN_TIER.value(), RaidTier.TIER_ONE).component(ModComponents.MAX_TIER.value(), RaidTier.TIER_SEVEN));
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        if (player == null || !(blockEntity instanceof RaidCrystalBlockEntity raidCrystal) || !blockState.is(ModBlocks.RAID_CRYSTAL_BLOCK.value())) return InteractionResult.PASS;
        if (!blockState.getValue(RaidCrystalBlock.CAN_RESET) || blockState.getValue(RaidCrystalBlock.WISH_USED)) {
            player.displayClientMessage(Component.translatable("item.cobblemonraiddens.wishing_piece.cannot").withStyle(ChatFormatting.RED), true);
            return InteractionResult.PASS;
        }

        ItemStack itemStack = context.getItemInHand();
        RaidTier min = itemStack.getOrDefault(ModComponents.MIN_TIER.value(), RaidTier.TIER_ONE);
        RaidTier max = itemStack.getOrDefault(ModComponents.MAX_TIER.value(), RaidTier.TIER_SEVEN);
        List<RaidTier> tiers = List.of(Arrays.copyOfRange(RaidTier.values(), min.ordinal(), max.ordinal() + 1));
        ResourceLocation bossRoll = CustomRaidRegistries.RAID_REGISTRY.getRandomRaidBoss(player.getRandom(), context.getLevel(), tiers, null, null, null);

        CustomRaidRegistries.RAID_REGISTRY.requestRaidBoss(bossRoll, boss -> {
            if (raidCrystal.isRemoved()) return;
            itemStack.consume(1, player);
            player.awardStat(Stats.ITEM_USED.get(this));
            CriteriaTriggers.CONSUME_ITEM.trigger(player, itemStack);
            raidCrystal.generateRaidBoss(boss, context.getLevel(), context.getClickedPos(), blockState, true);
        }, player.getServer());

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull TooltipContext context, List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.cobblemonraiddens.wishing_piece.tooltip").withStyle(ChatFormatting.GRAY));

        RaidTier min =itemStack.getOrDefault(ModComponents.MIN_TIER.value(), RaidTier.TIER_ONE);
        RaidTier max = itemStack.getOrDefault(ModComponents.MAX_TIER.value(), RaidTier.TIER_SEVEN);
        if (min == RaidTier.TIER_ONE && max == RaidTier.TIER_SEVEN) return;

        tooltip.add(Component.translatable("item.cobblemonraiddens.wishing_piece.tooltip.min", min.getStars()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.cobblemonraiddens.wishing_piece.tooltip.max", max.getStars()).withStyle(ChatFormatting.GRAY));
    }
}
