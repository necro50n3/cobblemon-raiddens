{
    use(battle, pokemon, itemId, data) {
        pokemon.clearBoosts();
        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('clear_boost')));
        battle.add('-clearboost', pokemon, '[from] Raid Energy');
    }
}
