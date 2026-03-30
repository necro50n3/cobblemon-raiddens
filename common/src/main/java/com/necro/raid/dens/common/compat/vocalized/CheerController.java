package com.necro.raid.dens.common.compat.vocalized;

import com.cobblemon.vocalized.api.voice.VoiceController;
import com.cobblemon.vocalized.api.voice.VoicePipeline;
import com.cobblemon.vocalized.common.client.VocalizedVoice;
import com.cobblemon.vocalized.common.voice.pipeline.VocalizedVoicePipeline;
import com.necro.raid.dens.common.items.item.CheerItem;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.showdown.bagitems.CheerBagItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class CheerController extends VoiceController {
    private final Supplier<VocalizedVoicePipeline> pipelineFactory = VocalizedVoice.INSTANCE::pipeline;
    public final String id;
    public final Holder<Item> cheer;
    public final List<String> phrases;

    public CheerController(String id, Holder<Item> cheer, String phrases) {
        this(id, cheer, List.of(phrases));
    }

    public CheerController(String id, Holder<Item> cheer, List<String> phrases) {
        super(() -> null);
        this.id = id;
        this.cheer = cheer;
        this.phrases = phrases;
    }

    @Override
    protected boolean isActive() {
        if (Minecraft.getInstance().player == null) return false;
        return Minecraft.getInstance().player.getInventory().hasAnyOf(Set.of(this.cheer.value()));
    }

    @Override
    public VoicePipeline buildPipeline() {
        return this.pipelineFactory.get().createSubstringMatchPipeline(this.phrases, string -> handle(), false);
    }

    private void handle() {
        CheerBagItem.CheerType cheerType = ((CheerBagItem) ((CheerItem) this.cheer.value()).getBagItem()).cheerType();
        RaidDenNetworkMessages.USE_CHEER.accept(cheerType);
    }

    @Override
    public @NotNull String threadName() {
        return "CobblemonRaidDens-" + this.id;
    }

    @Override
    public List<String> getPhrases() {
        return this.phrases;
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }
}
