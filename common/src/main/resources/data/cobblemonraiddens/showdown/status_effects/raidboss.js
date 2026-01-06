{
    name: 'raidboss',
    onDamage(damage, target, source, effect) {
        const name = effect.fullname === 'tox' ? 'psn' : effect.fullname;
        this.add('split', 'p2');
        switch (effect.id) {
        case 'strugglerecoil':
            this.add('-raiddamage', target, damage, '[from] Recoil');
            this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[from] Recoil');
            break;
        case 'partiallytrapped':
            this.add('-raiddamage', target, damage, '[from] ' + this.effectState.sourceEffect.fullname, '[partiallytrapped]');
            this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[from] Recoil');
            break;
        case 'powder':
            this.add('-raiddamage', target, damage, '[silent]');
            this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[silent]');
            break;
        case 'confused':
        case 'confusion':
            this.add('-raiddamage', target, damage, '[from] confusion');
            this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[from] confusion');
            break;
        default:
            if (effect.effectType === 'Move' || !name) {
                this.add('-raiddamage', target, damage);
                this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100));
            } else if (source && (source !== target || effect.effectType === 'Ability')) {
                this.add('-raiddamage', target, damage, '[from] ' + name, '[of] ' + source);
                this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[from] ' + name, '[of] ' + source);
            } else {
                this.add('-raiddamage', target, damage, '[from] ' + name);
                this.add('-raiddamage', target, Math.floor(damage / target.maxhp * 100), '[from] ' + name);
            }
            break;
        }
        return (target.hp === target.maxhp) ? 1 : 0;
    },
    onResidual(pokemon) {
        for (let i = 0; i < this.log.length - 2; i++) {
            if (this.log[i] === '|split|p2' && this.log[i + 1].startsWith('|-damage|p2a:') && this.log[i + 2].startsWith('|-damage|p2a:')) {
                this.log.splice(i, 3);
                i--;
            }
        }
    }
}