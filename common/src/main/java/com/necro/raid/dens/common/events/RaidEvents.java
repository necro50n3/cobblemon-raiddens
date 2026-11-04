package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.reactive.SimpleObservable;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.cobbledollars.RaidDensCobbleDollarsCompat;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.config.TierConfig;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.items.item.RaidPouchItem;
import com.necro.raid.dens.common.items.item.RaidShardItem;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.rewards.RewardHandler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class RaidEvents {
    // Raid Generation
    /**
     * An event that is fired when a raid boss is assigned to a raid den.
     */
    public static final SimpleObservable<SetRaidEvent> SET_RAID = new SimpleObservable<>();
    /**
     * An event that is fired when a raid den spawns.
     */
    public static final SimpleObservable<RaidDenSpawnEvent> RAID_DEN_SPAWN = new SimpleObservable<>();
    /**
     * An event that is fired when a raid den is loaded into the world.
     */
    public static final SimpleObservable<RaidDenSpawnEvent> RAID_DEN_LOAD = new SimpleObservable<>();
    /**
     * An event that is fired when a raid den is unloaded or saved from the world.
     */
    public static final SimpleObservable<RaidDenSpawnEvent> RAID_DEN_SAVE = new SimpleObservable<>();

    // Raid Battling
    /**
     * An event that is fired when a player joins a raid battle.
     */
    public static final ResultCancelableObservable<RaidJoinEvent> RAID_JOIN = new ResultCancelableObservable<>();
    /**
     * An event that is fired when a raid battle starts for a specific player.
     */
    public static final SimpleObservable<RaidBattleStartEvent> RAID_BATTLE_START = new SimpleObservable<>();
    /**
     * An event that is fired when a raid battle ends for a specific player.
     * <p>
     * Refer to the relevant events to modify or cancel specific events.
     * @see ModifyCatchRateEvent
     * @see ModifyShinyRateEvent
     * @see OpenPouchEvent
     * @see RaidRewardPostEvent
     * @see RewardPokemonEvent
     */
    public static final SimpleObservable<RaidBattleEndEvent> RAID_BATTLE_END = new SimpleObservable<>();
    /**
     * An event that is fired when a raid or raid adventure ends.
     */
    public static final SimpleObservable<RaidEndEvent> RAID_END = new SimpleObservable<>();

    // Raid Rewards
    /**
     * An event to modify the catch rate for a specific player when catching the raid boss.
     */
    public static final SimpleObservable<ModifyCatchRateEvent> MODIFY_CATCH_RATE = new SimpleObservable<>();
    /**
     * An event to modify the shiny rate for a specific player when catching the raid boss.
     */
    public static final SimpleObservable<ModifyShinyRateEvent> MODIFY_SHINY_RATE = new SimpleObservable<>();
    /**
     * An event that is fired when a player receives the reward Pokémon from the raid battle.
     */
    public static final ResultCancelableObservable<RewardPokemonEvent> REWARD_POKEMON = new ResultCancelableObservable<>();
    /**
     * An event that is fired after a player has received their rewards for a raid battle.
     */
    public static final SimpleObservable<RaidRewardPostEvent> RAID_REWARD_POST = new SimpleObservable<>();

    // Misc
    /**
     * An event to modify or cancel a player opening a raid pouch and receiving the rewards.
     *
     * @see RaidPouchItem
     */
    public static final ResultCancelableObservable<OpenPouchEvent> OPEN_POUCH = new ResultCancelableObservable<>();
    /**
     * An event that is fired when a player uses a raid shard with sufficient raid energy.
     * <p>
     * @see RaidShardItem
     */
    public static final ResultCancelableObservable<UseRaidShardEvent> USE_RAID_SHARD = new ResultCancelableObservable<>();
    /**
     * An event for when a player uses an item on a raid crystal.
     * <p>
     * Custom item interactions can be defined by subscribing to the event.
     * <p>
     * The event can then be canceled to prevent subsequent hooks and the default right-click behavior.
     */
    public static final ResultCancelableObservable<UseItemOnRaidCrystalEvent> USE_ITEM_ON_CRYSTAL = new ResultCancelableObservable<>();

    public static void registerEvents() {
        RaidEvents.RAID_JOIN.subscribe(Priority.NORMAL, event -> {
            RaidDenNetworkMessages.JOIN_RAID.accept(event.getPlayer(), true);
        });

        RaidEvents.RAID_BATTLE_END.subscribe(Priority.LOWEST, event -> {
            if (!event.isWin()) return;
            new RewardHandler(event.raidBoss(), event.player().getUUID(), event.pokemon(), event.itemReward(), event.catchRate()).sendRewardMessage(event.player());
        });

        RaidEvents.RAID_BATTLE_END.subscribe(Priority.LOWEST, event -> {
            if (!event.isWin()) return;

            Inventory inventory = event.player().getInventory();
            ItemStack raidShard = null;
            for (int i = 0; i <= Inventory.SLOT_OFFHAND; i++) {
                ItemStack itemStack = inventory.getItem(i);
                if (itemStack.is(ModItems.RAID_SHARD) && itemStack.getOrDefault(ModComponents.RAID_ENERGY.value(), 0) < CobblemonRaidDens.CONFIG.required_energy) {
                    raidShard = itemStack;
                    break;
                }
            }
            if (raidShard == null) return;

            TierConfig config = CobblemonRaidDens.TIER_CONFIG.get(event.raidBoss().getTier());
            raidShard.set(
                ModComponents.RAID_ENERGY.value(),
                Math.min(raidShard.getOrDefault(ModComponents.RAID_ENERGY.value(), 0) + config.energy(), CobblemonRaidDens.CONFIG.required_energy)
            );
        });

        RaidEvents.RAID_REWARD_POST.subscribe(Priority.LOWEST, event -> {
            if (!ModCompat.COBBLEDOLLARS.isLoaded()) return;
            else if (event.raidBoss().getCurrency() <= 0) return;
            RaidDensCobbleDollarsCompat.addCurrency(event.player(), event.raidBoss().getCurrency());
        });

        RaidEvents.USE_ITEM_ON_CRYSTAL.subscribe(Priority.NORMAL, event -> {
            if (!event.itemStack().is(ModItems.WISHING_PIECE)) return;
            event.itemStack().useOn(new UseOnContext(event.player(), event.interactionHand(), event.blockHitResult()));
            event.cancel();
        });
    }
}
