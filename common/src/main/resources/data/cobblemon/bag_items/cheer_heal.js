{
    use(battle, pokemon, itemId, data) {
        var ratio = parseFloat(data[0]);
        var cheerType = data[1];
                var origin = data[2];
        var amount = pokemon.heal(Math.floor(pokemon.maxhp * ratio));
        if (amount) {
            battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('cheer')));
            battle.add("cheer", cheerType, origin);
            battle.add('-heal', pokemon, pokemon.getHealth, '[from] bagitem: ' + itemId);
        }
    }
}
