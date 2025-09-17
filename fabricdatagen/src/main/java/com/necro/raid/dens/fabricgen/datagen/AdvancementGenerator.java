package com.necro.raid.dens.fabricgen.datagen;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.yajatkaul.mega_showdown.item.DynamaxItems;
import com.cobblemon.yajatkaul.mega_showdown.item.MegaStones;
import com.cobblemon.yajatkaul.mega_showdown.item.TeraMoves;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.advancements.RaidFeatureTrigger;
import com.necro.raid.dens.common.advancements.RaidTierTrigger;
import com.necro.raid.dens.common.advancements.JoinRaidDenTrigger;
import com.necro.raid.dens.common.advancements.RaidShinyTrigger;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.raids.RaidFeature;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import com.necro.raid.dens.fabricgen.blocks.FabricBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementGenerator extends FabricAdvancementProvider {
    public AdvancementGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer) {
        ItemStack display = new ItemStack(FabricBlocks.RAID_CRYSTAL_BLOCK);

        AdvancementHolder joinRaidDenAdvancement = Advancement.Builder.advancement()
            .display(
                display,
                Component.translatable("advancement.cobblemonraiddens.join_raid.title"),
                Component.translatable("advancement.cobblemonraiddens.join_raid.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.TASK,
                true, false, false
            )
            .addCriterion("joined_raid", JoinRaidDenTrigger.TriggerInstance.joinRaidDen())
            .save(consumer, CobblemonRaidDens.MOD_ID + ":join_raid");

        Advancement.Builder.advancement()
            .display(
                CobblemonItems.STAR_SWEET,
                Component.translatable("advancement.cobblemonraiddens.raid_shiny.title"),
                Component.translatable("advancement.cobblemonraiddens.raid_shiny.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.CHALLENGE,
                true, false, true
            )
            .parent(joinRaidDenAdvancement)
            .addCriterion("shiny_from_raid", RaidShinyTrigger.TriggerInstance.shiny(true))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":raid_shiny");

        ItemStack tierOneStack = display.copy();
        tierOneStack.set(ModComponents.TYPE_COMPONENT.value(), RaidType.NORMAL);
        AdvancementHolder tierOneAdvancement = Advancement.Builder.advancement()
            .display(
                tierOneStack,
                Component.translatable("advancement.cobblemonraiddens.tier_one.title"),
                Component.translatable("advancement.cobblemonraiddens.tier_one.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.GOAL,
                true, false, false
            )
            .parent(joinRaidDenAdvancement)
            .addCriterion("completed_tier_one", RaidTierTrigger.TriggerInstance.tier(RaidTier.TIER_ONE))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":tier_one");

        ItemStack tierTwoStack = display.copy();
        tierTwoStack.set(ModComponents.TYPE_COMPONENT.value(), RaidType.GRASS);
        AdvancementHolder tierTwoAdvancement = Advancement.Builder.advancement()
            .display(
                tierTwoStack,
                Component.translatable("advancement.cobblemonraiddens.tier_two.title"),
                Component.translatable("advancement.cobblemonraiddens.tier_two.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.GOAL,
                true, false, false
            )
            .parent(tierOneAdvancement)
            .addCriterion("completed_tier_two", RaidTierTrigger.TriggerInstance.tier(RaidTier.TIER_TWO))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":tier_two");

        ItemStack tierThreeStack = display.copy();
        tierThreeStack.set(ModComponents.TYPE_COMPONENT.value(), RaidType.FIRE);
        AdvancementHolder tierThreeAdvancement = Advancement.Builder.advancement()
            .display(
                tierThreeStack,
                Component.translatable("advancement.cobblemonraiddens.tier_three.title"),
                Component.translatable("advancement.cobblemonraiddens.tier_three.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.GOAL,
                true, false, false
            )
            .parent(tierTwoAdvancement)
            .addCriterion("completed_tier_three", RaidTierTrigger.TriggerInstance.tier(RaidTier.TIER_THREE))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":tier_three");

        ItemStack tierFourStack = display.copy();
        tierFourStack.set(ModComponents.TYPE_COMPONENT.value(), RaidType.WATER);
        AdvancementHolder tierFourAdvancement = Advancement.Builder.advancement()
            .display(
                tierFourStack,
                Component.translatable("advancement.cobblemonraiddens.tier_four.title"),
                Component.translatable("advancement.cobblemonraiddens.tier_four.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.GOAL,
                true, false, false
            )
            .parent(tierThreeAdvancement)
            .addCriterion("completed_tier_four", RaidTierTrigger.TriggerInstance.tier(RaidTier.TIER_FOUR))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":tier_four");

        ItemStack tierFiveStack = display.copy();
        tierFiveStack.set(ModComponents.TYPE_COMPONENT.value(), RaidType.GHOST);
        AdvancementHolder tierFiveAdvancement = Advancement.Builder.advancement()
            .display(
                tierFiveStack,
                Component.translatable("advancement.cobblemonraiddens.tier_five.title"),
                Component.translatable("advancement.cobblemonraiddens.tier_five.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.GOAL,
                true, false, false
            )
            .parent(tierFourAdvancement)
            .addCriterion("completed_tier_five", RaidTierTrigger.TriggerInstance.tier(RaidTier.TIER_FIVE))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":tier_five");

        ItemStack tierSixStack = display.copy();
        tierSixStack.set(ModComponents.TYPE_COMPONENT.value(), RaidType.DRAGON);
        AdvancementHolder tierSixAdvancement = Advancement.Builder.advancement()
            .display(
                tierSixStack,
                Component.translatable("advancement.cobblemonraiddens.tier_six.title"),
                Component.translatable("advancement.cobblemonraiddens.tier_six.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.CHALLENGE,
                true, false, true
            )
            .parent(tierFiveAdvancement)
            .addCriterion("completed_tier_six", RaidTierTrigger.TriggerInstance.tier(RaidTier.TIER_SIX))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":tier_six");

        Advancement.Builder.advancement()
            .display(
                display,
                Component.translatable("advancement.cobblemonraiddens.tier_seven.title"),
                Component.translatable("advancement.cobblemonraiddens.tier_seven.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.CHALLENGE,
                true, false, true
            )
            .parent(tierSixAdvancement)
            .addCriterion("completed_tier_seven", RaidTierTrigger.TriggerInstance.tier(RaidTier.TIER_SEVEN))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":tier_seven");

        AdvancementHolder teraRaidAchievement = Advancement.Builder.advancement()
            .display(
                TeraMoves.TERA_ORB,
                Component.translatable("advancement.cobblemonraiddens.raid_tera.title"),
                Component.translatable("advancement.cobblemonraiddens.raid_tera.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.GOAL,
                true, false, true
            )
            .parent(joinRaidDenAdvancement)
            .addCriterion("completed_tera", RaidFeatureTrigger.TriggerInstance.feature(RaidFeature.TERA))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":raid_tera");

        AdvancementHolder dynamaxRaidAchievement = Advancement.Builder.advancement()
            .display(
                DynamaxItems.DYNAMAX_BAND,
                Component.translatable("advancement.cobblemonraiddens.raid_dynamax.title"),
                Component.translatable("advancement.cobblemonraiddens.raid_dynamax.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.GOAL,
                true, false, true
            )
            .parent(teraRaidAchievement)
            .addCriterion("completed_dynamax", RaidFeatureTrigger.TriggerInstance.feature(RaidFeature.DYNAMAX))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":raid_dynamax");

        Advancement.Builder.advancement()
            .display(
                MegaStones.MEGA_STONE,
                Component.translatable("advancement.cobblemonraiddens.raid_mega.title"),
                Component.translatable("advancement.cobblemonraiddens.raid_mega.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.GOAL,
                true, false, true
            )
            .parent(dynamaxRaidAchievement)
            .addCriterion("completed_mega", RaidFeatureTrigger.TriggerInstance.feature(RaidFeature.MEGA))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":raid_mega");

        Advancement.Builder.advancement()
            .display(
                ModItems.HEAL_CHEER.value(),
                Component.translatable("advancement.cobblemonraiddens.great_friend.title"),
                Component.translatable("advancement.cobblemonraiddens.great_friend.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png"),
                AdvancementType.TASK,
                true, false, false
            )
            .parent(joinRaidDenAdvancement)
            .addCriterion("attack_cheer",
                ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.ATTACK_CHEER.value()))
            .addCriterion("defense_cheer",
                ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.DEFENSE_CHEER.value()))
            .addCriterion("heal_cheer",
                ConsumeItemTrigger.TriggerInstance.usedItem(ModItems.HEAL_CHEER.value()))
            .save(consumer, CobblemonRaidDens.MOD_ID + ":great_friend");
    }
}
