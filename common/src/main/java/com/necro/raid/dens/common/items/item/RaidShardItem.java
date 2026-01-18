package com.necro.raid.dens.common.items.item;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.util.ComponentUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class RaidShardItem extends Item {
    public RaidShardItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).component(ModComponents.RAID_ENERGY.value(), 0));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.getOrDefault(ModComponents.RAID_ENERGY.value(), 0) < CobblemonRaidDens.CONFIG.required_energy) {
            if (level.isClientSide()) player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid_shard.not_fully_charged"), true);
            return InteractionResultHolder.fail(itemStack);
        }

        if (!level.isClientSide()) {
            ItemStack raidCrystal = ModBlocks.INSTANCE.getRaidCrystalBlock().asItem().getDefaultInstance();
            player.setItemInHand(hand, raidCrystal);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
