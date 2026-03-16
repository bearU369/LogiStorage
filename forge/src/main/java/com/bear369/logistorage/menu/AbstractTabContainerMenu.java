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
import com.bear369.logistorage.menu.gui.IConfigurableSlot;
import com.bear369.logistorage.menu.gui.IMenuProvider;
import com.bear369.logistorage.menu.tab.AbstractTabContainer;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class AbstractTabContainerMenu extends AbstractMixContainerMenu implements IMenuProvider {
    private final Inventory playerInventory;
    private final BlockEntity parentEntity;
    private List<AbstractTabContainer> tabs = null;
    private int activeTab = 0;

    protected AbstractTabContainerMenu(MenuType<?> type, int id, Inventory inventory, BlockEntity parentEntity) {
        super(type, id, inventory.player);
        this.playerInventory = inventory;
        this.parentEntity = parentEntity;
    }

    public void registerTabs(List<AbstractTabContainer> tabs) {
        if (this.tabs != null) throw new IllegalStateException("AbstractTabContainer List is alreaady registered");

        this.tabs = tabs;
        this.init();
    }

    protected void init() {
        this.tabs.get(activeTab).layoutMenu(playerInventory, this);
    }

    private void clearTab() {
        this.slots.clear();
        this.fluidSlots.clear();
    }

    public void swapTabById(int index) {
        if (index < 0 || index >= this.tabs.size())
            throw new IllegalArgumentException("Index must be between 0 and %d".formatted(this.tabs.size()));

        this.activeTab = index;
        this.clearTab();
        this.init();
    }

    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        return this.tabs.get(activeTab).quickMoveStack(player, index, this);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        final Level currentlevel = Objects.requireNonNull(parentEntity.getLevel());
        return stillValid(
                ContainerLevelAccess.create(currentlevel, parentEntity.getBlockPos()),
                player,
                parentEntity.getBlockState().getBlock());
    }

    public AbstractTabContainer getActiveTabContainer() {
        return this.tabs.get(activeTab);
    }

    // === IMenuProvider === //
    @Override
    public void addSlot(IConfigurableSlot slot) {
        if (slot instanceof Slot itemSlot) {
            this.addSlot(itemSlot);
        } else if (slot instanceof FluidSlot fluidSlot) {
            this.addSlot(fluidSlot);
        }
    }

    @Override
    public boolean moveItemStackTo(@Nonnull ItemStack stack, int start, int end, boolean reverse) {
        return super.moveItemStackTo(stack, start, end, reverse);
    }

    @Override
    public List<Slot> getSlots() {
        return this.slots;
    }
    // === IMenuProvider === //
}
