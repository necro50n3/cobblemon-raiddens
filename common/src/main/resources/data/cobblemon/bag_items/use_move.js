{
    use(battle, pokemon, itemId, data) {
        var moveId = data[0];
        var target = data[1];
        pokemon.side.lastSelectedMove = battle.toID(moveId);
        battle.actions.runMove(moveId, pokemon, target, null, null, true);
    }
}
