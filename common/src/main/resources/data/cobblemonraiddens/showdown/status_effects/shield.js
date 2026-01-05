{
    name: 'shield',
    effectType: 'Status',
    onModifyDef(def, pokemon) {
        this.debug('Shield Reduction');
        def = this.finalModify(def);
        return Math.floor(def * 1.5);
    },
    onModifySpD(spd, pokemon) {
        this.debug('Shield Reduction');
        spd = this.finalModify(spd);
        return Math.floor(spd * 1.5);
    }
}