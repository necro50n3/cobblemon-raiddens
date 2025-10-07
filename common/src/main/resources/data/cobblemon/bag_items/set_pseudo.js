{
    use(battle, pokemon, itemId, data) {
        var pseudo = data[0];
        battle.field.addPseudoWeather(pseudo, pokemon);
        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('set_pseudo')));
    }
}
