package com.necro.raid.dens.neoforge.config;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.config.ClientConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;

public class ClientConfigScreen {
    public static Screen create(ModContainer container, Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("text.autoconfig.cobbleraiddens-client.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        builder.getOrCreateCategory(Component.literal("Beacon Beam"))
            .addEntry(entryBuilder.startBooleanToggle(
                Component.translatable("text.autoconfig.cobbleraiddens-client.option.show_beam_tier_one"),
                CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_one)
                .setDefaultValue(true)
                .setSaveConsumer(value -> CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_one = value)
                .build()
            )
            .addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.cobbleraiddens-client.option.show_beam_tier_two"),
                    CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_two)
                .setDefaultValue(true)
                .setSaveConsumer(value -> CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_two = value)
                .build()
            )
            .addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.cobbleraiddens-client.option.show_beam_tier_three"),
                    CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_three)
                .setDefaultValue(true)
                .setSaveConsumer(value -> CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_three = value)
                .build()
            )
            .addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.cobbleraiddens-client.option.show_beam_tier_four"),
                    CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_four)
                .setDefaultValue(true)
                .setSaveConsumer(value -> CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_four = value)
                .build()
            )
            .addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.cobbleraiddens-client.option.show_beam_tier_five"),
                    CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_five)
                .setDefaultValue(true)
                .setSaveConsumer(value -> CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_five = value)
                .build()
            )
            .addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.cobbleraiddens-client.option.show_beam_tier_six"),
                    CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_six)
                .setDefaultValue(true)
                .setSaveConsumer(value -> CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_six = value)
                .build()
            )
            .addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.cobbleraiddens-client.option.show_beam_tier_seven"),
                    CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_seven)
                .setDefaultValue(true)
                .setSaveConsumer(value -> CobblemonRaidDens.CLIENT_CONFIG.show_beam_tier_seven = value)
                .build()
            );

        builder.setSavingRunnable(() -> AutoConfig.getConfigHolder(ClientConfig.class).save());
        return builder.build();
    }
}
