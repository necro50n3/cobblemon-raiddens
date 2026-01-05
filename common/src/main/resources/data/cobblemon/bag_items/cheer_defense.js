{
    use(battle, pokemon, itemId, data) {
        var origin = data[0];
        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('cheer')));

        battle.add('cheer', 'cheer_defense', origin);
        for (let p of battle.sides[0].pokemon) {
            if (!p) continue;
            p.addVolatile('cheerdefense', p);
            battle.add('cheerboost', p);
        }
    }
}
