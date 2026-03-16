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
import com.bear369.logistorage.menu.gui.DynamicSlot;
import com.bear369.logistorage.menu.gui.IMenuProvider;
import com.bear369.logistorage.menu.gui.ITabProvider;
import com.bear369.logistorage.menu.gui.ItemButton;
import com.bear369.logistorage.menu.gui.MEPatternSlot;
import com.bear369.logistorage.shared.AE2;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class MEProcessInterfaceTabPattern extends AbstractTabContainer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/gui/container/interface_gui_pattern.png");

    public MEProcessInterfaceTabPattern(Container itemContainer) {
        super(itemContainer, null);
        this.imageWidth = 194;
        this.imageHeight = 168;
        this.inventoryLabelY = 75;
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, ITabProvider provider) {
        int x = (provider.getWidth() - this.imageWidth) / 2;
        int y = (provider.getHeight() - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blit(TEXTURE, provider.getLeftPos() + 173, provider.getTopPos() + 18, 244, 0, 12, 15);

        RenderSystem.setShaderColor(0.3f, 0.3f, 0.3f, 0.2f);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                guiGraphics.renderFakeItem(
                        new ItemStack(AE2.PROCESSING_PATTERN),
                        provider.getLeftPos() + 8 + 18 * col,
                        provider.getTopPos() + 18 + 18 * row);
            }
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void layoutScreen(ITabProvider provider) {
        ItemButton tmpBTN = new ItemButton(
                        provider.getLeftPos() - 20, provider.getTopPos(), 20, 20, Blocks.CHEST, (button) -> {
                            provider.swapTabFromID(0);
                        })
                .addComponent(Component.translatable("gui.logistorage.me_processing_interface.button.container")
                        .withStyle(ChatFormatting.WHITE))
                .addComponent(Component.translatable("gui.logistorage.me_processing_interface.button.container.tooltip")
                        .withStyle(ChatFormatting.GRAY));

        provider.addWidget(tmpBTN);
    }

    @Override
    public void layoutMenu(Inventory playerInventory, IMenuProvider provider) {
        // Player Inventory
        for (int col = 0; col < 9; col++) {
            provider.addSlot(new DynamicSlot(playerInventory, col, 8 + col * 18, 144));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                provider.addSlot(new DynamicSlot(playerInventory, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }
        // Container Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                provider.addSlot(new MEPatternSlot(itemContainer, col + row * 9, 8 + col * 18, 18 + row * 18));
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
