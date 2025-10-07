{
    use(battle, pokemon, itemId, data) {
        var origin = data[0];
        for (let i in pokemon.boosts) {
            if (pokemon.boosts[i] >= 0) continue; // Add check to skip positive boosts
            pokemon.boosts[i] = 0;
        }
        pokemon.cureStatus();

        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('clear_boost')));
        battle.add("raidenergy", origin);
        battle.add('clearboss', pokemon, origin);
    }
}
