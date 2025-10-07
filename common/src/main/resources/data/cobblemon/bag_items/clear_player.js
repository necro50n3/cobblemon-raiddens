{
    use(battle, pokemon, itemId, data) {
        var origin = data[0];
        for (let i in pokemon.boosts) {
            if (pokemon.boosts[i] <= 0) continue; // Add check to skip negative boosts
            pokemon.boosts[i] = 0;
        }

        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('clear_boost')));
        battle.add("raidenergy", origin);
        battle.add('clearplayer', pokemon, origin);
    }
}
