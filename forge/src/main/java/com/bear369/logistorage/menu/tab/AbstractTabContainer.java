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

import com.bear369.logistorage.menu.gui.IMenuProvider;
import com.bear369.logistorage.menu.gui.ITabProvider;
import com.bear369.logistorage.util.FluidContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractTabContainer {
    protected final Container itemContainer;
    protected final FluidContainer fluidContainer;
    public int imageWidth = 256;
    public int imageHeight = 256;
    public int inventoryLabelY = 0;

    public AbstractTabContainer(Container itemContainer, FluidContainer fluidContainer) {
        this.itemContainer = itemContainer;
        this.fluidContainer = fluidContainer;
    }

    public abstract void render(
            GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, ITabProvider provider);

    public abstract void layoutScreen(ITabProvider provider);

    public abstract void layoutMenu(Inventory playerInventory, IMenuProvider provider);

    public abstract ItemStack quickMoveStack(Player player, int index, IMenuProvider provider);
}
