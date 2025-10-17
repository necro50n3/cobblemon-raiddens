package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.reactive.SimpleObservable;

public class RaidEvents {
    public static final SimpleObservable<RaidBattleStartEvent> RAID_BATTLE_START = new SimpleObservable<>();
    public static final SimpleObservable<RaidEndEvent> RAID_END = new SimpleObservable<>();
    public static final SimpleObservable<RaidDenSpawnEvent> RAID_DEN_SPAWN = new SimpleObservable<>();
    public static final SimpleObservable<SetRaidBossEvent> SET_RAID_BOSS = new SimpleObservable<>();

    public static final ResultCancelableObservable<RaidJoinEvent> RAID_JOIN = new ResultCancelableObservable<>();
}
