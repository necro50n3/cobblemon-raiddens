package com.necro.raid.dens.common.client.gui;

import com.necro.raid.dens.common.CobblemonRaidDensClient;
import net.minecraft.util.Mth;

public enum GuiPosition {
    TOP,
    RIGHT,
    BOTTOM,
    LEFT;

    public int x(int maxX, int guiWidth) {
        int x = switch (this) {
            case LEFT -> 0;
            case TOP, BOTTOM -> (int) (maxX * CobblemonRaidDensClient.CLIENT_CONFIG.raid_status_offset / 100.0);
            case RIGHT -> maxX - guiWidth;
        };
        return Mth.clamp(x, 0, maxX - guiWidth);
    }

    public int y(int maxY, int guiHeight) {
        int y = switch (this) {
            case TOP -> 0;
            case RIGHT, LEFT -> (int) (maxY * CobblemonRaidDensClient.CLIENT_CONFIG.raid_status_offset / 100.0);
            case BOTTOM -> maxY - guiHeight;
        };
        return Mth.clamp(y, 0, maxY - guiHeight);
    }
}
