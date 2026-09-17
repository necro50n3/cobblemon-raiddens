package com.necro.raid.dens.common.loot.function;

import com.cobblemon.mod.common.api.tms.TechnicalMachine;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
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

public class TMTypeFunction extends LootItemConditionalFunction {
    public static final MapCodec<TMTypeFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> commonFields(instance)
        .apply(instance, TMTypeFunction::new));

    protected TMTypeFunction(List<LootItemCondition> list) {
        super(list);
    }

    @Override
    public @NotNull LootItemFunctionType<TMTypeFunction> getType() {
        return RaidLootFunctions.TM_TYPE_FUNCTION.value();
    }

    @Override
    protected @NotNull ItemStack run(@NotNull ItemStack itemStack, LootContext lootContext) {
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

        return this.getTM(raidType, lootContext.getRandom(), itemStack);
    }

    public static Builder<?> apply() {
        return simpleBuilder(TMTypeFunction::new);
    }

    private ItemStack getTM(RaidType raidType, RandomSource randomSource, ItemStack fallback) {
        ElementalType type = switch (raidType) {
            case NORMAL -> ElementalTypes.NORMAL;
            case FIGHTING -> ElementalTypes.FIGHTING;
            case FLYING -> ElementalTypes.FLYING;
            case POISON -> ElementalTypes.POISON;
            case GROUND -> ElementalTypes.GROUND;
            case ROCK -> ElementalTypes.ROCK;
            case BUG -> ElementalTypes.BUG;
            case GHOST -> ElementalTypes.GHOST;
            case STEEL -> ElementalTypes.STEEL;
            case FIRE -> ElementalTypes.FIRE;
            case WATER -> ElementalTypes.WATER;
            case GRASS -> ElementalTypes.GRASS;
            case ELECTRIC -> ElementalTypes.ELECTRIC;
            case PSYCHIC -> ElementalTypes.PSYCHIC;
            case ICE -> ElementalTypes.ICE;
            case DRAGON -> ElementalTypes.DRAGON;
            case DARK -> ElementalTypes.DARK;
            case FAIRY -> ElementalTypes.FAIRY;
            default -> null;
        };
        TechnicalMachine[] tms = TechnicalMachine.Companion.filterTms(null, type, null, null, true).toArray(TechnicalMachine[]::new);
        if (tms.length == 0) return fallback;
        int index = randomSource.nextInt(tms.length);
        return tms[index].createItemStack();
    }
}
