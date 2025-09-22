{
    use(battle, pokemon, itemId, data) {
        var origin = data[0];
        for (let i in this.boosts) {
            if (this.boosts[i] <= 0) continue; // Add check to skip negative boosts
            this.boosts[i] = 0;

            if (i === 'evasion' || i === 'accuracy') continue;
            this.modifiedStats[i] = this.storedStats[i];
        }

        battle.log = battle.log.filter(line => !(line.startsWith('|bagitem|') && line.includes('clear_boost')));
        battle.add("raidenergy", origin);
        battle.add('clearplayer', pokemon, origin);
    }
}
