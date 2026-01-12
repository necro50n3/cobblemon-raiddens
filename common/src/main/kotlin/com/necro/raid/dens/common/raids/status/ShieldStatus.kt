package com.necro.raid.dens.common.raids.status

import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.necro.raid.dens.common.CobblemonRaidDens
import net.minecraft.resources.ResourceLocation

class ShieldStatus : PersistentStatus(
    name = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "shield"),
    showdownName = "shield",
    applyMessage = "cobblemonraiddens.status.shield.apply",
    removeMessage = "cobblemonraiddens.status.shield.cure",
    defaultDuration = IntRange(180, 300)
)