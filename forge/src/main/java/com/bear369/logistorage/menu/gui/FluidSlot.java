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

import com.bear369.logistorage.util.FluidContainer;
import net.minecraftforge.fluids.FluidStack;

public class FluidSlot implements IConfigurableSlot {
    public final FluidContainer container;
    public final int x, y, width, height;
    private final int slot;

    public FluidSlot(FluidContainer container, int slot, int x, int y, int width, int height) {
        this.container = container;
        this.slot = slot;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public FluidSlot(FluidContainer container, int slot, int x, int y) {
        this(container, slot, x, y, 16, 16);
    }

    public FluidStack getFluid() {
        return this.container.getFluid(this.slot);
    }

    public int getCapacity() {
        return this.container.getCapacityInSlot(slot);
    }

    public int getSlotIndex() {
        return this.slot;
    }

    @Override
    public boolean isHovering(double mouseX, double mouseY) {
        return mouseX >= (double) (x - 1)
                && mouseX < (double) (x + width)
                && mouseY >= (double) (y - 1)
                && mouseY < (double) (y + height);
    }

    public boolean isHighlightable() {
        return true;
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
