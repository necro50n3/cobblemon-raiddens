package com.necro.raid.dens.common.compat.wthit;

import com.mojang.blaze3d.vertex.PoseStack;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.component.ItemComponent;
import mcp.mobius.waila.api.component.WrappedComponent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ScalableComponent implements ITooltipComponent {
    private final ITooltipComponent component;
    private float scale;

    public ScalableComponent(ITooltipComponent component, float scale) {
        super();
        this.component = component;
        this.scale = scale;
    }

    public ScalableComponent(ItemStack stack, float scale) {
        this(new ItemComponent(stack), scale);
    }

    public ScalableComponent(Component component, float scale) {
        this(new WrappedComponent(component), scale);
    }

    public ScalableComponent scale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public int getWidth() {
        return (int) (this.component.getWidth() * this.scale);
    }

    @Override
    public int getHeight() {
        return (int) (this.component.getHeight() * this.scale);
    }

    @Override
    public void render(GuiGraphics ctx, int x, int y, DeltaTracker delta) {
        if (this.component instanceof ItemComponent itemComponent) {
            this.renderItem(ctx, x + 1, y + 1, itemComponent.stack);
        }
        else {
            PoseStack poseStack = ctx.pose();
            poseStack.pushPose();
            poseStack.translate(x * this.scale, y * this.scale, 0);
            poseStack.scale(this.scale, this.scale, this.scale);
            this.component.render(ctx, x, y, delta);
            poseStack.popPose();
        }
    }

    private void renderItem(GuiGraphics ctx, int x, int y, ItemStack stack) {
        PoseStack poseStack = ctx.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0F);
        poseStack.scale(scale, scale, scale);
        ctx.renderFakeItem(stack, 0, 0);
        ctx.renderItemDecorations(Minecraft.getInstance().font, stack, x, y, "");
        poseStack.popPose();
    }
}
