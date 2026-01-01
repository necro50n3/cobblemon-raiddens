package com.necro.raid.dens.common.commands.permission;

import com.cobblemon.mod.common.api.permission.Permission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record RaidDenPermission(String node, PermissionLevel level) implements Permission {
    @Override
    public @NotNull ResourceLocation getIdentifier() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, this.node);
    }

    @Override
    public @NotNull String getLiteral() {
        return CobblemonRaidDens.MOD_ID + "." + this.node;
    }

    @Override
    public @NotNull PermissionLevel getLevel() {
        return this.level;
    }
}
