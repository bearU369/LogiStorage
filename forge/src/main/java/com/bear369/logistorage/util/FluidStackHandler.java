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

import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidStackHandler implements IFluidHandler {
    protected NonNullList<FluidStack> stacks;

    public FluidStackHandler(int size) {
        if (size <= 0) throw new IllegalArgumentException();
        this.stacks = NonNullList.withSize(size, FluidStack.EMPTY);
    }

    public FluidStackHandler() {
        this(1);
    }

    public @Nonnull CompoundTag serializeNBT() {
        final CompoundTag nbt = new CompoundTag();
        final ListTag stackList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            final FluidStack currentStack = this.stacks.get(i);
            if (currentStack.isEmpty()) continue;
            final CompoundTag fluidStackTag = currentStack.writeToNBT(new CompoundTag());
            fluidStackTag.putInt("Slot", i);
            stackList.add(fluidStackTag);
        }
        nbt.put("Fluids", stackList);
        return nbt;
    }

    public void deserializeNBT(CompoundTag tag) {
        final ListTag stackList = tag.contains("Fluids") ? tag.getList("Fluids", Tag.TAG_COMPOUND) : new ListTag();
        if (stackList.size() <= 0) return;

        for (int i = 0; i < stackList.size(); i++) {
            final CompoundTag fluidStackTag = stackList.getCompound(i);
            final FluidStack stack = FluidStack.loadFluidStackFromNBT(fluidStackTag);
            final int slot = fluidStackTag.getInt("Slot");
            if (slot >= 0 && slot < stacks.size()) this.stacks.set(slot, stack);
        }
    }

    @Override
    public int getTanks() {
        return this.stacks.size();
    }

    @Override
    public @Nonnull FluidStack getFluidInTank(int slot) {
        validateRange(slot);
        return this.stacks.get(slot);
    }

    @Override
    public int getTankCapacity(int slot) {
        return 10000;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int fillCount = 0;
        for (int i = 0; i < this.getTanks(); i++) {
            fillCount = this.fill(i, resource, action);
            if (fillCount > 0) return fillCount;
        }
        return 0;
    }

    public int fill(int slot, FluidStack stack, FluidAction action) {
        if (stack.isEmpty()) return 0;

        if (!this.isFluidValid(slot, stack)) return 0;

        validateRange(slot);

        final FluidStack existing = this.stacks.get(slot);
        int limit = this.getTankCapacity(slot);

        if (!existing.isEmpty()) {
            if (!FluidHandlerHelper.canFluidStacksStack(stack, existing)) return 0;
            limit -= existing.getAmount();
        }

        if (limit <= 0) return 0;

        boolean reachedLimit = stack.getAmount() > limit;
        if (action == FluidAction.EXECUTE) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? FluidHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getAmount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? limit : stack.getAmount();
    }

    public void setFluidInSlot(int slot, @Nonnull FluidStack stack) {
        Objects.requireNonNull(stack);
        this.stacks.set(slot, stack);
        this.onContentsChanged(slot);
    }

    @Override
    public @Nonnull FluidStack drain(FluidStack resource, FluidAction action) {
        for (int i = 0; i < this.getTanks(); i++) {
            if (this.stacks.get(i).getRawFluid().isSame(resource.getRawFluid()))
                return this.drain(i, resource.getAmount(), action);
        }
        return FluidStack.EMPTY;
    }

    public @Nonnull FluidStack drain(int slot, int amount, FluidAction action) {
        if (amount <= 0) return FluidStack.EMPTY;

        validateRange(slot);
        FluidStack existing = this.stacks.get(slot);

        if (existing.isEmpty()) return FluidStack.EMPTY;

        int toExtract = Math.min(amount, existing.getAmount());

        if (existing.getAmount() <= toExtract) {
            if (action == FluidAction.EXECUTE) {
                this.stacks.set(slot, FluidStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (action == FluidAction.EXECUTE) {
                this.stacks.set(slot, FluidHandlerHelper.copyStackWithSize(existing, existing.getAmount() - toExtract));
                onContentsChanged(slot);
            }

            return FluidHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public @Nonnull FluidStack drain(int maxDrain, FluidAction action) {
        for (int i = 0; i < this.getTanks(); i++) {
            if (!this.stacks.get(i).isEmpty()) return this.drain(i, maxDrain, action);
        }
        return FluidStack.EMPTY;
    }

    protected void onContentsChanged(int slot) {}

    private void validateRange(int i) {
        if (i < 0 || i >= this.stacks.size())
            throw new IllegalArgumentException("index must be between 0 and %d".formatted(this.stacks.size()));
    }
}
