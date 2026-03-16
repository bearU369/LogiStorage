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

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class DynamicSlot extends Slot implements IConfigurableSlot {

    public DynamicSlot(Container container, int slotIndex, int slotX, int slotY) {
        super(container, slotIndex, slotX, slotY);
    }

    @Override
    public boolean isHovering(double mouseX, double mouseY) {
        return mouseX >= (double) (this.x - 1)
                && mouseX < (double) (this.x + 16)
                && mouseY >= (double) (this.y - 1)
                && mouseY < (double) (this.y + 16);
    }
}
