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
package com.bear369.logistorage.shared.ae2.containers;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import com.bear369.logistorage.entity.AENetworkBlockEntity;
import com.bear369.logistorage.util.ItemContainer;
import net.minecraft.world.item.ItemStack;

public class ReturnItemInvContainer extends ItemContainer {
    private final AENetworkBlockEntity host;

    public ReturnItemInvContainer(int size, AENetworkBlockEntity host) {
        super(size);
        this.host = host;
    }

    @Override
    public void setChanged() {
        this.refreshState();
    }

    public void refreshState() {
        if (!super.isEmpty())
            host.ifPresent((grid, node) -> {
                grid.getTickManager().alertDevice(node);
            });
    }

    public boolean tickWork(IActionSource src) {
        final MEStorage storageService =
                host.getGridNode(null).getGrid().getStorageService().getInventory();
        boolean doWork = false;
        for (int i = 0; i < super.getContainerSize(); i++) {
            final ItemStack currentStack = super.getItem(i);
            if (currentStack == ItemStack.EMPTY || currentStack.isEmpty()) continue;
            final GenericStack stack = GenericStack.fromItemStack(currentStack);
            final long insertedCount = storageService.insert(stack.what(), stack.amount(), Actionable.MODULATE, src);

            final long adjustedAmount = stack.amount() - insertedCount;
            if (adjustedAmount <= 0) {
                super.setItem(i, ItemStack.EMPTY);
            } else {
                super.setItem(i, new ItemStack(currentStack.getItem(), (int) adjustedAmount));
            }
            doWork = true;
        }
        return doWork;
    }
}
