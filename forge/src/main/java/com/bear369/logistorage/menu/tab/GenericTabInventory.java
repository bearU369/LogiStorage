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
package com.bear369.logistorage.menu.tab;

import com.bear369.logistorage.core.Main;
import com.bear369.logistorage.menu.gui.DisabledFluidSlot;
import com.bear369.logistorage.menu.gui.DisabledSlot;
import com.bear369.logistorage.menu.gui.DynamicSlot;
import com.bear369.logistorage.menu.gui.FluidSlot;
import com.bear369.logistorage.menu.gui.IMenuProvider;
import com.bear369.logistorage.menu.gui.ITabProvider;
import com.bear369.logistorage.util.FluidContainer;
import com.bear369.logistorage.util.GUIRenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GenericTabInventory extends AbstractTabContainer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/container/interface_gui_1.png");

    public GenericTabInventory(Container itemContainer, FluidContainer fluidContainer) {
        super(itemContainer, fluidContainer);
        this.imageWidth = 198;
        this.imageHeight = 245;
        this.inventoryLabelY = 152;
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, ITabProvider provider) {
        int x = (provider.getWidth() - this.imageWidth) / 2;
        int y = (provider.getHeight() - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blit(TEXTURE, provider.getLeftPos() + 174, provider.getTopPos() + 18, 244, 0, 12, 15);
        guiGraphics.blit(TEXTURE, provider.getLeftPos() + 174, provider.getTopPos() + 77, 244, 0, 12, 15);

        for (FluidSlot slot : provider.getFluidSlots()) {
            GUIRenderUtil.renderFluid(
                    guiGraphics,
                    slot.getFluid(),
                    provider.getLeftPos() + slot.x,
                    provider.getTopPos() + slot.y,
                    slot.width,
                    slot.height,
                    slot.getCapacity());
        }
        provider.onHoverRender(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void layoutScreen(ITabProvider provider) {}

    @Override
    public void layoutMenu(Inventory playerInventory, IMenuProvider provider) {
        // Player Inventory
        for (int col = 0; col < 9; col++) { // Player's Tool bar
            provider.addSlot(new DynamicSlot(playerInventory, col, 8 + col * 18, 221));
        }
        for (int row = 0; row < 3; row++) { // Player's inventory
            for (int col = 0; col < 9; col++) {
                provider.addSlot(new DynamicSlot(playerInventory, col + row * 9 + 9, 8 + col * 18, 163 + row * 18));
            }
        }
        // Container's item inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                final int slotIndex = col + row * 9;
                if (slotIndex < itemContainer.getContainerSize()) {
                    provider.addSlot(new DynamicSlot(itemContainer, slotIndex, 8 + col * 18, 18 + row * 18));
                } else {
                    provider.addSlot(new DisabledSlot(itemContainer, 8 + col * 18, 18 + row * 18));
                }
            }
        }
        // Container's fluid inventory
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                final int slotIndex = col + row * 9;
                if (slotIndex < fluidContainer.getTanks()) {
                    provider.addSlot(new FluidSlot(fluidContainer, slotIndex, 8 + col * 18, 77 + row * 36, 16, 34));
                } else {
                    provider.addSlot(
                            new DisabledFluidSlot(fluidContainer, slotIndex, 8 + col * 18, 77 + row * 36, 16, 34));
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index, IMenuProvider provider) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = provider.getSlots().get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack existing = slot.getItem();
            itemStack = existing.copy();

            if (index < 36) { // PLAYER SLOTS
                if (!provider.moveItemStackTo(existing, 36, provider.getSlots().size(), false)) return ItemStack.EMPTY;
            } else { // CONTAINER SLOTS
                if (!provider.moveItemStackTo(existing, 0, 36, false)) return ItemStack.EMPTY;
            }

            if (existing.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (existing.getCount() == itemStack.getCount()) return ItemStack.EMPTY;

            slot.onTake(player, existing);
        }
        return itemStack;
    }
}
