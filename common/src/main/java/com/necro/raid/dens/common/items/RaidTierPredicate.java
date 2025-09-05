package com.necro.raid.dens.common.items;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.raids.RaidTier;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;

public record RaidTierPredicate(RaidTier tier) implements ItemSubPredicate {
    public static final Codec<RaidTierPredicate> CODEC;

    public boolean matches(ItemStack itemStack) {
        CobblemonRaidDens.LOGGER.info("Predicate works, checking item stack");
        RaidTier itemTier = itemStack.get(ModComponents.TIER_COMPONENT.value());
        if (itemTier == null) {
            CobblemonRaidDens.LOGGER.info("Tier not found in item stack");
            return false;
        }
        CobblemonRaidDens.LOGGER.info("Tier found, comparing values");
        return this.tier == itemTier;
    }

    public static RaidTierPredicate ofTier(RaidTier tier) {
        return new RaidTierPredicate(tier);
    }

    public RaidTier tier() {
        return this.tier;
    }

    static {
        CODEC = RaidTier.codec().xmap(RaidTierPredicate::new, RaidTierPredicate::tier);
    }
}
