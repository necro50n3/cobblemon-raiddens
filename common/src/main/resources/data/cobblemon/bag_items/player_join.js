{
    use(battle, pokemon, itemId, data) {
        var newHp = parseInt(data[0]);
        var player = data[1];
        pokemon.hp = newHp;

        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('player_join')));
        battle.add("playerjoin", pokemon, player);
    }
}