package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.events.Cancelable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

 /**
  * An event for when a player uses an item on a raid crystal.
  * <p>
  * Custom item interactions can be defined by subscribing to the event.
  * <p>
  * The event can then be canceled to prevent subsequent hooks and the default right-click behavior.
  */
public class UseItemOnRaidCrystalEvent extends Cancelable {
    private final ItemStack itemStack;
    private final BlockState blockState;
    private final ServerLevel level;
    private final BlockPos blockPos;
    private final ServerPlayer player;
    private final InteractionHand interactionHand;
    private final BlockHitResult blockHitResult;

    public UseItemOnRaidCrystalEvent(@NotNull ItemStack itemStack, @NotNull BlockState blockState, ServerLevel level, @NotNull BlockPos blockPos, @NotNull ServerPlayer player, @NotNull InteractionHand interactionHand, @NotNull BlockHitResult blockHitResult) {
        this.itemStack = itemStack;
        this.blockState = blockState;
        this.level = level;
        this.blockPos = blockPos;
        this.player = player;
        this.interactionHand = interactionHand;
        this.blockHitResult = blockHitResult;
    }

    public ItemStack itemStack() {
        return this.itemStack;
    }

    public BlockState blockState() {
        return this.blockState;
    }

    public ServerLevel level() {
        return this.level;
    }

    public BlockPos blockPos() {
        return this.blockPos;
    }

    public ServerPlayer player() {
        return this.player;
    }

    public InteractionHand interactionHand() {
        return this.interactionHand;
    }

    public BlockHitResult blockHitResult() {
        return this.blockHitResult;
    }
}
