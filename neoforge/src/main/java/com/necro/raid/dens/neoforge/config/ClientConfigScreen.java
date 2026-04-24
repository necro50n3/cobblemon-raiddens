package com.necro.raid.dens.neoforge.config;

import com.necro.raid.dens.common.CobblemonRaidDensClient;
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
            .setTitle(Component.translatable("text.autoconfig.cobblemonraiddens/client.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        builder.getOrCreateCategory(Component.translatable("text.autoconfig.cobblemonraiddens/client.category.beacon_beam"))
            .addEntry(entryBuilder.startIntSlider(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.beam_strength_tier_one"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_one, 0, 255)
                .setDefaultValue(32)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_one = value)
                .build()
            )
            .addEntry(entryBuilder.startIntSlider(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.beam_strength_tier_two"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_two, 0, 255)
                .setDefaultValue(64)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_two = value)
                .build()
            )
            .addEntry(entryBuilder.startIntSlider(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.beam_strength_tier_three"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_three, 0, 255)
                .setDefaultValue(96)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_three = value)
                .build()
            )
            .addEntry(entryBuilder.startIntSlider(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.beam_strength_tier_four"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_four, 0, 255)
                .setDefaultValue(128)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_four = value)
                .build()
            )
            .addEntry(entryBuilder.startIntSlider(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.beam_strength_tier_five"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_five, 0, 255)
                .setDefaultValue(160)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_five = value)
                .build()
            )
            .addEntry(entryBuilder.startIntSlider(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.beam_strength_tier_six"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_six, 0, 255)
                .setDefaultValue(192)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_six = value)
                .build()
            )
            .addEntry(entryBuilder.startIntSlider(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.beam_strength_tier_seven"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_seven, 0, 255)
                .setDefaultValue(224)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_seven = value)
                .build()
            )
            .addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.show_particles"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.show_particles)
                .setDefaultValue(true)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.show_particles = value)
                .build()
            )
            .addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.show_legacy_beacon"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.show_legacy_beacon)
                .setDefaultValue(false)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.show_legacy_beacon = value)
                .build()
            );

        builder.getOrCreateCategory(Component.translatable("text.autoconfig.cobblemonraiddens/client.category.raiding"))
            .addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.auto_accept_requests"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.auto_accept_requests)
                .setDefaultValue(true)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.auto_accept_requests = value)
                .build()
            )
            .addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.enable_raid_logs"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.enable_raid_logs)
                .setDefaultValue(true)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.enable_raid_logs = value)
                .build()
            )
            .addEntry(entryBuilder.startBooleanToggle(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.enable_health_bars"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.enable_health_bars)
                .setDefaultValue(true)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.enable_health_bars = value)
                .build()
            );

        builder.getOrCreateCategory(Component.translatable("text.autoconfig.cobblemonraiddens/client.category.gui"))
            .addEntry(entryBuilder.startIntSlider(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.raid_status_x"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.raid_status_x, 0, 100)
                .setDefaultValue(100)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.raid_status_x = value)
                .build()
            )
            .addEntry(entryBuilder.startIntSlider(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.raid_status_y"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.raid_status_y, 0, 100)
                .setDefaultValue(50)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.raid_status_y = value)
                .build()
            )
            .addEntry(entryBuilder.startIntSlider(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.raid_popup_x"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.raid_popup_x, 0, 100)
                .setDefaultValue(50)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.raid_popup_x = value)
                .build()
            )
            .addEntry(entryBuilder.startIntSlider(
                    Component.translatable("text.autoconfig.cobblemonraiddens/client.option.raid_popup_y"),
                    CobblemonRaidDensClient.CLIENT_CONFIG.raid_popup_y, 0, 100)
                .setDefaultValue(77)
                .setSaveConsumer(value -> CobblemonRaidDensClient.CLIENT_CONFIG.raid_popup_y = value)
                .build()
            );

        builder.setSavingRunnable(() -> AutoConfig.getConfigHolder(ClientConfig.class).save());
        return builder.build();
    }
}
