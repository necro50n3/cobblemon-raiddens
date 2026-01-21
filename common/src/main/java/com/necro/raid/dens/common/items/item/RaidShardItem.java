package com.necro.raid.dens.common.items.item;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.client.tooltip.ProgressTooltipData;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.util.ComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class RaidShardItem extends Item {
    public RaidShardItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).component(ModComponents.RAID_ENERGY.value(), 0));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (getRaidEnergy(itemStack) < CobblemonRaidDens.CONFIG.required_energy) {
            if (level.isClientSide()) player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid_shard.not_fully_charged"), true);
            return InteractionResultHolder.fail(itemStack);
        }

        if (!level.isClientSide()) {
            ItemStack raidCrystal = ModBlocks.INSTANCE.getRaidCrystalBlock().asItem().getDefaultInstance();
            player.setItemInHand(hand, raidCrystal);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack itemStack) {
        return Optional.of(new ProgressTooltipData(getRaidEnergy(itemStack) / ((float) CobblemonRaidDens.CONFIG.required_energy)));
    }

    private static int getRaidEnergy(ItemStack itemStack) {
        return itemStack.getOrDefault(ModComponents.RAID_ENERGY.value(), 0);
    }
}
