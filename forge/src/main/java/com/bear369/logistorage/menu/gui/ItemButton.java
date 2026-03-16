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
package com.bear369.logistorage.menu.gui;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemButton extends Button {
    private final ItemStack itemStack;
    private List<Component> components = new ArrayList<>();

    public ItemButton(int x, int y, int width, int height, ItemStack itemStack, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.itemStack = itemStack;
    }

    public ItemButton(int x, int y, int width, int height, Item item, OnPress onPress) {
        this(x, y, width, height, new ItemStack(item), onPress);
    }

    public ItemButton(int x, int y, int width, int height, Block block, OnPress onPress) {
        this(x, y, width, height, block.asItem(), onPress);
    }

    @Override
    protected void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        final int itemX = this.getX() + (this.width / 2) - 8;
        final int itemY = this.getY() + (this.height / 2) - 8;

        guiGraphics.renderFakeItem(this.itemStack, itemX, itemY);

        if (this.isHovered()) {
            final Font font = Minecraft.getInstance().font;

            if (this.components.isEmpty()) {
                guiGraphics.renderTooltip(font, this.itemStack, mouseX, mouseY);
            } else {
                guiGraphics.renderComponentTooltip(font, this.components, mouseX, mouseY);
            }
        }
    }

    public ItemButton addComponent(Component component) {
        if (component == null) return this;
        this.components.add(component);
        return this;
    }
}
