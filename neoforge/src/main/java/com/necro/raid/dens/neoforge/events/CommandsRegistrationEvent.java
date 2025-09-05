package com.necro.raid.dens.neoforge.events;

import com.mojang.brigadier.CommandDispatcher;
import com.necro.raid.dens.common.commands.RaidAdminCommands;
import com.necro.raid.dens.common.commands.RaidRequestCommands;
import com.necro.raid.dens.common.commands.RaidRewardCommands;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommandsRegistrationEvent {
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        RaidAdminCommands.register(dispatcher);
        RaidRequestCommands.register(dispatcher);
        RaidRewardCommands.register(dispatcher, event.getBuildContext());
    }
}
