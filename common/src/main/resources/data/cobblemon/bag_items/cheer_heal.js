{
    use(battle, pokemon, itemId, data) {
        var ratio = parseFloat(data[0]);
        var amount = pokemon.heal(Math.floor(pokemon.maxhp * ratio));
        if (amount) {
            battle.add('-heal', pokemon, pokemon.getHealth, '[from] bagitem: ' + itemId);
        }
    }
}
