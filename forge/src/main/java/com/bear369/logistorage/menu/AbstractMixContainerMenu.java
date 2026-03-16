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

import com.bear369.logistorage.menu.gui.FluidSlot;
import com.bear369.logistorage.network.FluidSlotInitial;
import com.bear369.logistorage.network.FluidSlotSyncPacket;
import com.bear369.logistorage.network.NetworkHandler;
import com.bear369.logistorage.util.FluidHandlerHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fluids.FluidStack;

/**
 * Extension of {@link AbstractContainerMenu} for adding fluid-based slots
 */
public abstract class AbstractMixContainerMenu extends AbstractContainerMenu {
    public final NonNullList<FluidSlot> fluidSlots = NonNullList.create();
    protected final Player player;

    private final NonNullList<FluidStack> remoteStacks =
            NonNullList.create(); // For syncing Fluid slots between Client and Server

    protected AbstractMixContainerMenu(MenuType<?> menu, int id, Player player) {
        super(menu, id);
        this.player = player;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (player instanceof ServerPlayer serverPlayer) {
            for (int i = 0; i < fluidSlots.size(); i++) {
                final FluidStack currentStack = this.fluidSlots.get(i).getFluid();
                final FluidStack currentRemote = this.remoteStacks.get(i);
                if (!FluidHandlerHelper.matches(currentStack, currentRemote)) {
                    this.remoteStacks.set(i, currentStack.copy());
                    NetworkHandler.sendToPlayer(new FluidSlotSyncPacket(i, currentStack), serverPlayer);
                }
            }
        }
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();

        if (player instanceof ServerPlayer serverPlayer) {
            final List<FluidStack> stacks = new ArrayList<>(this.fluidSlots.size());
            for (int i = 0; i < this.fluidSlots.size(); i++) {
                FluidStack copy = this.fluidSlots.get(i).getFluid().copy();
                this.remoteStacks.set(i, copy);
                stacks.add(copy);
            }
            NetworkHandler.sendToPlayer(new FluidSlotInitial(stacks), serverPlayer);
        }
    }

    protected FluidSlot addSlot(FluidSlot slot) {
        this.fluidSlots.add(slot);
        this.remoteStacks.add(FluidStack.EMPTY);
        return slot;
    }
}
