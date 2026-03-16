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
package com.bear369.logistorage.util;

import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ItemContainer implements Container {

    protected final ItemStackHandler handler;
    private final LazyOptional<IItemHandler> handlerCap;

    public ItemContainer(int size) {
        if (size <= 0) throw new IllegalArgumentException();
        this.handler = new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.handlerCap = LazyOptional.of(() -> handler);
    }

    public ItemContainer() {
        this(1);
    }

    public @Nonnull CompoundTag serializeNBT() {
        final CompoundTag containerTag = new CompoundTag();
        final CompoundTag handlerNBT = this.handler.serializeNBT();
        containerTag.put("Handler", handlerNBT != null ? handlerNBT : new CompoundTag());
        return containerTag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Handler")) this.handler.deserializeNBT(tag.getCompound("Handler"));
    }

    public final LazyOptional<IItemHandler> getCapability() {
        return this.handlerCap;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.getContainerSize(); i++) this.handler.setStackInSlot(i, ItemStack.EMPTY);
        setChanged();
    }

    @Override
    public int getContainerSize() {
        return this.handler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.getContainerSize(); i++) {
            if (!this.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.handler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        setChanged();
        return this.handler.extractItem(slot, count, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.handler.extractItem(slot, this.handler.getSlotLimit(slot), false);
    }

    @Override
    public void setItem(int slot, @Nonnull ItemStack stack) {
        this.handler.setStackInSlot(slot, stack);
        setChanged();
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return true;
    }
}
