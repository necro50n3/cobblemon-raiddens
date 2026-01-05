{
    use(battle, pokemon, itemId, data) {
        var origin = data[0];
        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('cheer')));

        battle.add('cheer', 'cheer_attack', origin);
        for (let p of battle.sides[0].pokemon) {
            if (!p) continue;
            p.addVolatile('cheerattack', p);
            battle.add('cheerboost', p);
        }
    }
}
