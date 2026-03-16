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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

/**
 * @implNote Container for {@link FluidStackHandler}
 */
public class FluidContainer {
    protected final FluidStackHandler handler;
    private final LazyOptional<FluidStackHandler> handlerCap;

    public FluidContainer(int size) {
        this.handler = new FluidStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.handlerCap = LazyOptional.of(() -> handler);
    }

    public FluidContainer() {
        this(1);
    }

    public @Nonnull CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Handler", handler.serializeNBT());
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Handler")) this.handler.deserializeNBT(tag.getCompound("Handler"));
    }

    public void setChanged() {}

    public final LazyOptional<FluidStackHandler> getCapability() {
        return this.handlerCap;
    }

    public int getTanks() {
        return this.handler.getTanks();
    }

    public int getCapacityInSlot(int slot) {
        return this.handler.getTankCapacity(slot);
    }

    public List<FluidStack> convertToFluidStackList() {
        final List<FluidStack> newList = new ArrayList<>(this.getTanks());
        for (int i = 0; i < this.getTanks(); i++) {
            newList.add(this.getFluid(i));
        }
        return newList;
    }

    public boolean isEmpty() {
        for (FluidStack stack : this.handler.stacks) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    public int fillInSlot(int slot, FluidStack stack, FluidAction action) {
        int fillCount = this.handler.fill(slot, stack, action);
        return fillCount;
    }

    public FluidStack drainInSlot(int slot, int amount, FluidAction action) {
        FluidStack drainStack = this.handler.drain(slot, amount, action);
        return drainStack;
    }

    public void setFluid(int slot, @Nonnull FluidStack stack) {
        Objects.nonNull(stack);
        this.handler.setFluidInSlot(slot, stack);
    }

    public FluidStack getFluid(int slot) {
        return this.handler.getFluidInTank(slot);
    }

    public void clearContent() {
        for (int i = 0; i < this.getTanks(); i++) {
            this.setFluid(i, FluidStack.EMPTY);
        }
    }
}
