{
    use(battle, pokemon, itemId, data) {
        var weather = data[0];
        battle.field.setWeather(weather, pokemon);
        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('set_weather')));
    }
}
