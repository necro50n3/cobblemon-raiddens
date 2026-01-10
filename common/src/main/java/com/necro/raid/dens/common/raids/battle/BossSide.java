package com.necro.raid.dens.common.raids.battle;

public class BossSide extends RaidSide {
    public final RaidPokemon pokemon;

    public BossSide(int side) {
        super(side);
        this.pokemon = new RaidPokemon();
    }
}
