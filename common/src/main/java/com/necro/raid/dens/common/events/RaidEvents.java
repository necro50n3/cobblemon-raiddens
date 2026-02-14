package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.reactive.SimpleObservable;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.config.TierConfig;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.RewardHandler;
import kotlin.Unit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class RaidEvents {
    public static final SimpleObservable<RaidBattleStartEvent> RAID_BATTLE_START = new SimpleObservable<>();
    public static final SimpleObservable<RaidEndEvent> RAID_END = new SimpleObservable<>();
    public static final SimpleObservable<RaidDenSpawnEvent> RAID_DEN_SPAWN = new SimpleObservable<>();
    public static final SimpleObservable<SetRaidBossEvent> SET_RAID_BOSS = new SimpleObservable<>();

    public static final ResultCancelableObservable<RaidJoinEvent> RAID_JOIN = new ResultCancelableObservable<>();
    public static final ResultCancelableObservable<RewardPokemonEvent> REWARD_POKEMON = new ResultCancelableObservable<>();
    public static final ResultCancelableObservable<OpenPouchEvent> OPEN_POUCH = new ResultCancelableObservable<>();
    public static final ResultCancelableObservable<UseRaidShardEvent> USE_RAID_SHARD = new ResultCancelableObservable<>();

    public static void registerEvents() {
        RaidEvents.RAID_JOIN.subscribe(Priority.NORMAL, event -> {
            RaidDenNetworkMessages.JOIN_RAID.accept(event.getPlayer(), true);
            return Unit.INSTANCE;
        });

        RaidEvents.RAID_END.subscribe(Priority.LOWEST, event -> {
            if (!event.isWin()) return;
            new RewardHandler(event.getRaidBoss(), event.getPlayer().getUUID(), event.getPokemon()).sendRewardMessage(event.getPlayer());
        });

        RaidEvents.RAID_END.subscribe(Priority.LOWEST, event -> {
            if (!event.isWin()) return Unit.INSTANCE;

            Inventory inventory = event.getPlayer().getInventory();
            ItemStack raidShard = null;
            for (int i = 0; i <= Inventory.SLOT_OFFHAND; i++) {
                ItemStack itemStack = inventory.getItem(i);
                if (itemStack.is(ModItems.RAID_SHARD) && itemStack.getOrDefault(ModComponents.RAID_ENERGY.value(), 0) < CobblemonRaidDens.CONFIG.required_energy) {
                    raidShard = itemStack;
                    break;
                }
            }
            if (raidShard == null) return Unit.INSTANCE;

            TierConfig config = CobblemonRaidDens.TIER_CONFIG.get(event.getRaidBoss().getTier());
            raidShard.set(
                ModComponents.RAID_ENERGY.value(),
                Math.min(raidShard.getOrDefault(ModComponents.RAID_ENERGY.value(), 0) + config.energy(), CobblemonRaidDens.CONFIG.required_energy)
            );

            return Unit.INSTANCE;
        });
    }
}
