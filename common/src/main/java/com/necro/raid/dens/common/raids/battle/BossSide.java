package com.necro.raid.dens.common.raids.battle;

public class BossSide extends RaidSide {
    public final RaidPokemon pokemon;

    public BossSide() {
        super(2);
        this.pokemon = new RaidPokemon();
    }
}
