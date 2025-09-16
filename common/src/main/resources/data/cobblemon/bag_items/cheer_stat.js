{
    /* cheer_stat atk spa 1 */
    use(battle, pokemon, itemId, data) {
        var stat1 = data[0];
        var stat2 = data[1];
        var boosts = {};
        boosts[stat1] = parseInt(data[2]);
        boosts[stat2] = parseInt(data[2]);
        var cheerType = data[3];
        var origin = data[4];
        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('cheer')));
        battle.add("cheer", cheerType, origin);
        battle.boost(boosts, pokemon, null, { effectType: 'BagItem', name: itemId });
    }
}
