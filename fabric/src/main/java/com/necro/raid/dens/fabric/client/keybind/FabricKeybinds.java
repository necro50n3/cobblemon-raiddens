package com.necro.raid.dens.fabric.client.keybind;

import com.necro.raid.dens.common.client.keybind.RaidDenKeybinds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class FabricKeybinds {
    public static void registerKeybinds() {
        KeyBindingHelper.registerKeyBinding(RaidDenKeybinds.MOUSE_KEYDOWN);
        KeyBindingHelper.registerKeyBinding(RaidDenKeybinds.ACCEPT_SHORTCUT);
        KeyBindingHelper.registerKeyBinding(RaidDenKeybinds.DENY_SHORTCUT);

        ClientTickEvents.END_CLIENT_TICK.register(client -> RaidDenKeybinds.handleKeyInput());
    }
}
