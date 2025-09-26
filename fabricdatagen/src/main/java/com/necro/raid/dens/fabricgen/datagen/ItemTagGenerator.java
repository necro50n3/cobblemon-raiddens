package com.necro.raid.dens.fabricgen.datagen;

import com.necro.raid.dens.common.items.ItemTags;
import com.necro.raid.dens.common.items.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends FabricTagProvider.ItemTagProvider {
    public ItemTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(ItemTags.RAID_DEN_KEY);
        getOrCreateTagBuilder(ItemTags.CHEERS)
            .add(ModItems.ATTACK_CHEER.value())
            .add(ModItems.DEFENSE_CHEER.value())
            .add(ModItems.HEAL_CHEER.value());
    }
}
