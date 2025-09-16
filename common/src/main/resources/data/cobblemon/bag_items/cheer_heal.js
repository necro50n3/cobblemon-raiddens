{
    use(battle, pokemon, itemId, data) {
        var ratio = parseFloat(data[0]);
        var origin = data[1];
        var amount = pokemon.heal(Math.floor(pokemon.maxhp * ratio));
        if (amount) {
            battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('clear_boost')));
            battle.add("cheer", itemId, origin);
            battle.add('-heal', pokemon, pokemon.getHealth, '[from] bagitem: ' + itemId);
        }
    }
}
