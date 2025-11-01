package com.necro.raid.dens.common.items.item;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.events.OpenPouchEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.raids.RaidFeature;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RaidPouchItem extends Item {
    public RaidPouchItem() {
        super(new Properties().stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        RaidTier tier = itemStack.get(ModComponents.TIER_COMPONENT.value());
        RaidFeature feature = itemStack.get(ModComponents.FEATURE_COMPONENT.value());
        RaidType raidType = itemStack.get(ModComponents.TYPE_COMPONENT.value());
        if (tier == null || feature == null  ||raidType == null ) return InteractionResultHolder.fail(itemStack);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            List<ItemStack> rewards = this.getRewardItems(tier, feature, (ServerLevel) level, player);
            if (!RaidEvents.OPEN_POUCH.postWithResult(new OpenPouchEvent((ServerPlayer) player, itemStack, rewards))) {
                return InteractionResultHolder.fail(itemStack);
            }

            for (ItemStack item : this.getRewardItems(tier, feature, (ServerLevel) level, player)) {
                if (!player.getInventory().add(item)) {
                    ItemEntity itemEntity = player.drop(item, false);
                    if (itemEntity == null) continue;
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setTarget(player.getUUID());
                }
            }

            player.awardStat(Stats.ITEM_USED.get(this));
            itemStack.consume(1, player);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        RaidTier tier = stack.get(ModComponents.TIER_COMPONENT.value());
        RaidFeature feature = stack.get(ModComponents.FEATURE_COMPONENT.value());
        if (tier == null || feature == null) return;
        tooltip.add(Component.translatable(feature.getTranslatable()).append(" | ").append(tier.getStars()));
    }

    private List<ItemStack> getRewardItems(RaidTier tier, RaidFeature feature, ServerLevel level, Player player) {
        List<ItemStack> rewards = new ArrayList<>(this.getTierRewards(tier, level, player));
        rewards.addAll(this.getFeatureRewards(feature, level, player));
        return rewards;
    }

    private List<ItemStack> getTierRewards(RaidTier tier, ServerLevel level, Player player) {
        return level.getServer().reloadableRegistries()
            .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, tier.getLootTableId())))
            .getRandomItems(new LootParams.Builder(level).withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSet.builder().optional(LootContextParams.THIS_ENTITY).build()));
    }

    private List<ItemStack> getFeatureRewards(RaidFeature feature, ServerLevel level, Player player) {
        return level.getServer().reloadableRegistries()
            .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, feature.getLootTableId())))
            .getRandomItems(new LootParams.Builder(level).withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .create(LootContextParamSet.builder().optional(LootContextParams.THIS_ENTITY).build()));
    }
}
