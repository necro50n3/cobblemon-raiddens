{
    use(battle, pokemon, itemId, data) {
        var terrain = data[0];
        battle.field.setTerrain(terrain, pokemon);
        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('set_terrain')));
    }
}
