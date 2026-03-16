/**
 * Copyright (C) 2026 @bear_369
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.bear369.logistorage.menu.screen;

import com.bear369.logistorage.menu.AbstractMixContainerMenu;
import com.bear369.logistorage.menu.gui.DisabledFluidSlot;
import com.bear369.logistorage.menu.gui.DisabledSlot;
import com.bear369.logistorage.menu.gui.FluidSlot;
import com.bear369.logistorage.network.FluidSlotClickPacket;
import com.bear369.logistorage.network.NetworkHandler;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.fluids.FluidStack;

public abstract class AbstractMixContainerScreen<T extends AbstractMixContainerMenu>
        extends AbstractContainerScreen<T> {

    public AbstractMixContainerScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderTooltip(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        final FluidSlot fluidSlot = this.findSlot(mouseX, mouseY);
        if (fluidSlot != null) {
            if (fluidSlot.getFluid() == FluidStack.EMPTY) return;
            final List<Component> tooltips = new ArrayList<>();

            final String fluidName = fluidSlot.getFluid().getDisplayName().getString();
            final int fluidAmount = fluidSlot.getFluid().getAmount();
            final int fluidCap = fluidSlot.getCapacity();

            tooltips.add(Component.literal(fluidName).withStyle(ChatFormatting.WHITE));
            tooltips.add(
                    Component.literal(fluidAmount + " / " + fluidCap + " mB").withStyle(ChatFormatting.GRAY));

            guiGraphics.renderComponentTooltip(this.font, tooltips, mouseX, mouseY);
        }
    }

    protected void renderDisabled(GuiGraphics guiGraphics) {
        for (Slot slot : this.menu.slots) {
            if (slot instanceof DisabledSlot) {
                int x = this.leftPos + slot.x;
                int y = this.topPos + slot.y;
                guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 16, 0x80404040);
            }
        }
        for (FluidSlot slot : this.menu.fluidSlots) {
            if (slot instanceof DisabledFluidSlot) {
                int x = this.leftPos + slot.x;
                int y = this.topPos + slot.y;
                guiGraphics.fill(RenderType.guiOverlay(), x, y, x + slot.width, y + slot.height, 0x80404040);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        final FluidSlot slot = this.findSlot(mouseX, mouseY);
        if (slot != null && slot.isActive()) {
            NetworkHandler.sendToServer(new FluidSlotClickPacket(slot.getSlotIndex()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected FluidSlot findSlot(double mouseX, double mouseY) {
        final double x = mouseX - (double) this.leftPos;
        final double y = mouseY - (double) this.topPos;
        for (FluidSlot fluidSlot : this.menu.fluidSlots) {
            if (fluidSlot.isHovering(x, y) && fluidSlot.isActive()) return fluidSlot;
        }
        return null;
    }

    protected void onHoverRender(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        final FluidSlot fluidSlot = this.findSlot(mouseX, mouseY);
        if (fluidSlot != null) {
            int x = this.leftPos + fluidSlot.x;
            int y = this.topPos + fluidSlot.y;
            guiGraphics.fillGradient(
                    RenderType.guiOverlay(),
                    x,
                    y,
                    x + fluidSlot.width,
                    y + fluidSlot.height,
                    getSlotColor(0),
                    getSlotColor(0),
                    y + fluidSlot.height);
        }
    }
}
