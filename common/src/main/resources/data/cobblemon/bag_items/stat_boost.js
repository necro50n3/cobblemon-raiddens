{
    use(battle, pokemon, itemId, data) {
        var stat1 = data[0];
        var boosts = {};
        boosts[stat1] = parseInt(data[1]);
        var origin = data[2];
        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('stat_boost')));
        battle.boost(boosts, pokemon, null, { effectType: 'BagItem', name: itemId });
    }
}
