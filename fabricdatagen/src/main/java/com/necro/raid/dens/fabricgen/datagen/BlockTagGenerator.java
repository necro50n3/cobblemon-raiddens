package com.necro.raid.dens.fabricgen.datagen;

import com.necro.raid.dens.common.blocks.BlockTags;
import com.necro.raid.dens.common.blocks.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends FabricTagProvider.BlockTagProvider {
    public BlockTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.CAN_INTERACT)
            .add(ModBlocks.INSTANCE.getRaidHomeBlock())
            .addOptional(ResourceLocation.fromNamespaceAndPath("yigd", "grave"))
            .addOptional(ResourceLocation.fromNamespaceAndPath("gravestone", "gravestone"));
    }
}
