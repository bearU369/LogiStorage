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

import javax.annotation.Nonnull;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DisabledSlot extends Slot implements IConfigurableSlot {

    public DisabledSlot(Container inv, int slotX, int slotY) {
        super(inv, -1, slotX, slotY);
    }

    @Override
    public boolean isHovering(double mouseX, double mouseY) {
        return mouseX >= (double) (this.x - 1)
                && mouseX < (double) (this.x + 16)
                && mouseY >= (double) (this.y - 1)
                && mouseY < (double) (this.y + 16);
    }

    @Override
    public void set(@Nonnull ItemStack stack) {}

    @Override
    public void setChanged() {}

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public ItemStack remove(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(@Nonnull Player player) {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }
}
