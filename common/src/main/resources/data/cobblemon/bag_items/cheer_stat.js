{
    /* cheer_stat atk spa 1 */
    use(battle, pokemon, itemId, data) {
    console.log(data);
        var stat1 = data[0];
        var stat2 = data[1];
        var boosts = {};
        boosts[stat1] = parseInt(data[2]);
        boosts[stat2] = parseInt(data[2]);
        battle.boost(boosts, pokemon, null, { effectType: 'BagItem', name: itemId });
    }
}
