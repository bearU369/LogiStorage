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
package com.bear369.logistorage.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GenericTabContainerMenu extends AbstractTabContainerMenu {

    public GenericTabContainerMenu(int id, Inventory inventory, BlockEntity parentEntity) {
        super(com.bear369.logistorage.core.MenuProvider.GENERIC_TAB_CONTAINER_MENU.get(), id, inventory, parentEntity);
    }

    public static GenericTabContainerMenu createMenu(int windowId, Inventory inv, FriendlyByteBuf data) {
        final BlockPos pos = data.readBlockPos();
        final BlockEntity entity = inv.player.level().getBlockEntity(pos);
        if (entity instanceof MenuProvider provider)
            return (GenericTabContainerMenu) provider.createMenu(windowId, inv, inv.player);
        throw new IllegalStateException("Expected menu provider at " + pos);
    }
}
