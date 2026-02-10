package com.necro.raid.dens.common.data.raid;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.loot.context.RaidLootContexts;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;

public class BossLootTable {
    public static final Codec<BossLootTable> DIRECT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.BOOL.fieldOf("replace").orElse(false).forGetter(BossLootTable::replace),
        ResourceLocation.CODEC.fieldOf("id").forGetter(BossLootTable::lootTable)
    ).apply(inst, BossLootTable::new));

    public static final Codec<BossLootTable> CODEC = Codec.either(ResourceLocation.CODEC, DIRECT_CODEC)
        .xmap(either ->
                either.map(BossLootTable::new, data -> data),
            data -> {
                if (!data.replace()) return Either.left(data.lootTable());
                else return Either.right(data);
            });

    private final boolean replace;
    private final ResourceLocation lootTable;
    private LootTable cachedLootTable;

    public BossLootTable(boolean replace, ResourceLocation lootTable) {
        this.replace = replace;
        this.lootTable = lootTable;
        this.cachedLootTable = null;
    }

    public BossLootTable(ResourceLocation lootTable) {
        this(false, lootTable);
    }

    public boolean replace() {
        return this.replace;
    }

    private ResourceLocation lootTable() {
        return this.lootTable;
    }

    public List<ItemStack> getRandomRewards(ServerLevel level, ItemStack itemStack, Player player) {
        if (this.lootTable == null) return new ArrayList<>();
        if (this.cachedLootTable == null) {
            this.cachedLootTable = level.getServer().reloadableRegistries().getLootTable(
                ResourceKey.create(Registries.LOOT_TABLE, this.lootTable)
            );
        }
        return this.cachedLootTable.getRandomItems(
            new LootParams.Builder(level)
                .withParameter(RaidLootContexts.RAID_POUCH, itemStack)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .create(RaidLootContexts.RAID_POUCH_USE)
        );
    }

    public void clearCache() {
        this.cachedLootTable = null;
    }
}
