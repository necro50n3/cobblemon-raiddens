package com.necro.raid.dens.common.loot.function;

import com.cobblemon.mod.common.CobblemonItems;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.loot.LootFunctions;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GemTypeFunction extends LootItemConditionalFunction {
    private static ItemStack[] GEMS = null;

    public static final MapCodec<GemTypeFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> commonFields(instance)
        .apply(instance, GemTypeFunction::new));

    protected GemTypeFunction(List<LootItemCondition> list) {
        super(list);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return (LootItemFunctionType<? extends LootItemConditionalFunction>) LootFunctions.GEM_TYPE_FUNCTION.value();
    }

    @Override
    protected @NotNull ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity == null) return itemStack;
        if (!(entity instanceof LivingEntity livingEntity)) return itemStack;
        ItemStack heldItem = livingEntity.getMainHandItem();

        RaidTier raidTier;
        if (heldItem.has(ModComponents.TIER_COMPONENT.value())) {
            raidTier = heldItem.get(ModComponents.TIER_COMPONENT.value());
        }
        else return itemStack;

        RaidType raidType;
        if (heldItem.has(ModComponents.TYPE_COMPONENT.value())) {
            raidType = heldItem.get(ModComponents.TYPE_COMPONENT.value());
        }
        else return itemStack;
        assert raidTier != null && raidType != null;
        if (raidType == RaidType.NONE) return itemStack;

        itemStack = this.getGem(raidType, lootContext.getRandom());
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> apply() {
        return simpleBuilder(GemTypeFunction::new);
    }

    private ItemStack getRandomGem(RandomSource randomSource) {
        if (GEMS == null) {
            GEMS = new ItemStack[]{
                CobblemonItems.FIGHTING_GEM.getDefaultInstance(),
                CobblemonItems.FLYING_GEM.getDefaultInstance(),
                CobblemonItems.POISON_GEM.getDefaultInstance(),
                CobblemonItems.GROUND_GEM.getDefaultInstance(),
                CobblemonItems.ROCK_GEM.getDefaultInstance(),
                CobblemonItems.BUG_GEM.getDefaultInstance(),
                CobblemonItems.GHOST_GEM.getDefaultInstance(),
                CobblemonItems.STEEL_GEM.getDefaultInstance(),
                CobblemonItems.FIRE_GEM.getDefaultInstance(),
                CobblemonItems.WATER_GEM.getDefaultInstance(),
                CobblemonItems.GRASS_GEM.getDefaultInstance(),
                CobblemonItems.ELECTRIC_GEM.getDefaultInstance(),
                CobblemonItems.PSYCHIC_GEM.getDefaultInstance(),
                CobblemonItems.ICE_GEM.getDefaultInstance(),
                CobblemonItems.DRAGON_GEM.getDefaultInstance(),
                CobblemonItems.DARK_GEM.getDefaultInstance(),
                CobblemonItems.FAIRY_GEM.getDefaultInstance(),
                CobblemonItems.NORMAL_GEM.getDefaultInstance()
            };
        }
        int rand = randomSource.nextInt(0, 18);
        return GEMS[rand];
    }

    private ItemStack getGem(RaidType raidType, RandomSource randomSource) {
        return switch (raidType) {
            case NORMAL -> CobblemonItems.NORMAL_GEM.getDefaultInstance();
            case FIGHTING -> CobblemonItems.FIGHTING_GEM.getDefaultInstance();
            case FLYING -> CobblemonItems.FLYING_GEM.getDefaultInstance();
            case POISON -> CobblemonItems.POISON_GEM.getDefaultInstance();
            case GROUND -> CobblemonItems.GROUND_GEM.getDefaultInstance();
            case ROCK -> CobblemonItems.ROCK_GEM.getDefaultInstance();
            case BUG -> CobblemonItems.BUG_GEM.getDefaultInstance();
            case GHOST -> CobblemonItems.GHOST_GEM.getDefaultInstance();
            case STEEL -> CobblemonItems.STEEL_GEM.getDefaultInstance();
            case FIRE -> CobblemonItems.FIRE_GEM.getDefaultInstance();
            case WATER -> CobblemonItems.WATER_GEM.getDefaultInstance();
            case GRASS -> CobblemonItems.GRASS_GEM.getDefaultInstance();
            case ELECTRIC -> CobblemonItems.ELECTRIC_GEM.getDefaultInstance();
            case PSYCHIC -> CobblemonItems.PSYCHIC_GEM.getDefaultInstance();
            case ICE -> CobblemonItems.ICE_GEM.getDefaultInstance();
            case DRAGON -> CobblemonItems.DRAGON_GEM.getDefaultInstance();
            case DARK -> CobblemonItems.DARK_GEM.getDefaultInstance();
            case FAIRY -> CobblemonItems.FAIRY_GEM.getDefaultInstance();
            default -> getRandomGem(randomSource);
        };
    }
}
