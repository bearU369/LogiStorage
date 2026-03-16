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
import net.minecraftforge.fluids.FluidStack;

public final class FluidHandlerHelper {

    public static boolean canFluidStacksStack(@Nonnull FluidStack a, @Nonnull FluidStack b) {
        if (a.isEmpty() || !a.isFluidEqual(b) || a.hasTag() != b.hasTag()) {
            return false;
        }
        return (!a.hasTag() || a.getTag().equals(b.getTag())); // && a.areFluidStackTagsEqual(a, a)
    }

    public static boolean matches(@Nonnull FluidStack a, @Nonnull FluidStack b) {
        return a.isFluidEqual(b) && a.getAmount() == b.getAmount();
    }

    public static FluidStack copyStackWithSize(@Nonnull FluidStack stack, int size) {
        if (size == 0) return FluidStack.EMPTY;
        FluidStack copy = stack.copy();
        copy.setAmount(size);
        return copy;
    }

    protected FluidHandlerHelper() {}
}
