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
package com.bear369.logistorage.entity;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener.State;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import com.bear369.logistorage.core.BlockEntityTypeRegistry;
import com.bear369.logistorage.menu.AbstractTabContainerMenu;
import com.bear369.logistorage.menu.GenericTabContainerMenu;
import com.bear369.logistorage.menu.tab.AbstractTabContainer;
import com.bear369.logistorage.menu.tab.MEProcessInterfaceTabInventory;
import com.bear369.logistorage.menu.tab.MEProcessInterfaceTabPattern;
import com.bear369.logistorage.shared.ComputerCraft;
import com.bear369.logistorage.shared.ae2.ICraftingProcessListener;
import com.bear369.logistorage.shared.ae2.containers.PatternContainer;
import com.bear369.logistorage.shared.ae2.containers.ReturnFluidInvContainer;
import com.bear369.logistorage.shared.ae2.containers.ReturnItemInvContainer;
import com.bear369.logistorage.shared.ae2.services.CraftingEventService;
import com.bear369.logistorage.shared.computercraft.peripherial.MEProcessInterfacePeripherialLogic;
import com.bear369.logistorage.util.FluidContainer;
import com.bear369.logistorage.util.ItemContainer;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

public class MEProcessInterfaceEntity extends AENetworkBlockEntity implements IActionHost, MenuProvider {
    private final MEProcessInterfacePeripherialLogic abilityCC = new MEProcessInterfacePeripherialLogic(this);

    private final FluidContainer fluidContainer = new FluidContainer(18);
    private final ItemContainer itemContainer = new ItemContainer(27);
    private final PatternContainer patternContainer =
            new PatternContainer(this, 27, this.itemContainer, this.fluidContainer);

    private final ReturnItemInvContainer returnInvContainer =
            new ReturnItemInvContainer(this.itemContainer.getContainerSize(), this);
    private final ReturnFluidInvContainer returnTankContainer =
            new ReturnFluidInvContainer(this.fluidContainer.getTanks(), this);

    private final LazyOptional<IPeripheral> peripheralCap = LazyOptional.of(() -> abilityCC);

    public MEProcessInterfaceEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeRegistry.ME_PROCESSING_INTERFACE.get(), pos, state);
        this.addService(IGridTickable.class, new Ticker());
        this.addService(ICraftingProcessListener.class, abilityCC);
        this.addService(ICraftingProvider.class, this.patternContainer);

        this.patternContainer.addListener(abilityCC);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ComputerCraft.CAPABILITY_PERIPHERIAL) {
            return peripheralCap.cast();
        } else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return returnInvContainer.getCapability().cast();
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return returnTankContainer.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public IGridNode getActionableNode() {
        return super.getMainNode();
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();
        final CraftingEventService eventService = CraftingEventService.getService(getActionableNode());

        eventService.addNode(getMainNode(), getPersistentData());
    }

    @Override
    public void setRemoved() {
        detachFromService();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        detachFromService();
        super.onChunkUnloaded();
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("FluidContainerTag", this.fluidContainer.serializeNBT());
        tag.put("ItemContainerTag", this.itemContainer.serializeNBT());
        tag.put("PatternContainerTag", this.patternContainer.serializeNBT());
        tag.put("ReturnItemInvContainerTag", this.returnInvContainer.serializeNBT());
        tag.put("ReturnFluidInvContainerTag", this.returnTankContainer.serializeNBT());
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("FluidContainerTag")) this.fluidContainer.deserializeNBT(tag.getCompound("FluidContainerTag"));
        if (tag.contains("ItemContainerTag")) this.itemContainer.deserializeNBT(tag.getCompound("ItemContainerTag"));
        if (tag.contains("PatternContainerTag"))
            this.patternContainer.deserializeNBT(tag.getCompound("PatternContainerTag"));
        if (tag.contains("ReturnItemInvContainerTag"))
            this.returnInvContainer.deserializeNBT(tag.getCompound("ReturnItemInvContainerTag"));
        if (tag.contains("ReturnFluidInvContainerTag"))
            this.returnTankContainer.deserializeNBT(tag.getCompound("ReturnFluidInvContainerTag"));
    }

    @Override
    protected void onStateChanged(IGridNode node, State state) {
        if (this.getActionableNode().isOnline()) {
            attachToService();
            patternContainer.refreshState();
            returnInvContainer.refreshState();
            returnTankContainer.refreshState();
        } else {
            detachFromService();
        }
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) {
        AbstractTabContainerMenu menu = new GenericTabContainerMenu(id, inventory, this);
        menu.registerTabs(this.getTabContainers());
        return menu;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.logistorage.me_processing_interface");
    }

    public List<AbstractTabContainer> getTabContainers() {
        final List<AbstractTabContainer> tabs = new ArrayList<>();
        tabs.add(new MEProcessInterfaceTabInventory(this.itemContainer, this.fluidContainer));
        tabs.add(new MEProcessInterfaceTabPattern(this.patternContainer));
        return tabs;
    }

    public ItemContainer getExposedItemInventory() {
        return this.itemContainer;
    }

    public ItemContainer getExposedReturnInventory() {
        return this.returnInvContainer;
    }

    public ItemContainer getExposedPatternContainer() {
        return this.patternContainer;
    }

    public FluidContainer getExposedFluidContainer() {
        return this.fluidContainer;
    }

    public IPeripheral getPeripheral() {
        return this.abilityCC;
    }

    // === UTILITIES ==//
    private void attachToService() {
        if (this.getActionableNode() == null) return;

        final CraftingEventService eventService = CraftingEventService.getService(getActionableNode());
        eventService.addNode(getMainNode(), getPersistentData());
    }

    private void detachFromService() {
        if (this.getActionableNode() == null) return;

        final CraftingEventService eventService = CraftingEventService.getService(getActionableNode());
        eventService.removeNode(getMainNode());
    }

    private class Ticker implements IGridTickable {
        private Ticker() {}

        private boolean getWorkStatus() {
            return MEProcessInterfaceEntity.this.patternContainer.isBusy()
                    || !MEProcessInterfaceEntity.this.patternContainer.isInitialized()
                    || !MEProcessInterfaceEntity.this.returnInvContainer.isEmpty()
                    || !MEProcessInterfaceEntity.this.returnTankContainer.isEmpty();
        }

        private boolean doWork() {
            return MEProcessInterfaceEntity.this.patternContainer.initCraftingCPU()
                    || MEProcessInterfaceEntity.this.patternContainer.tickWork()
                    || MEProcessInterfaceEntity.this.returnInvContainer.tickWork(
                            IActionSource.ofMachine(MEProcessInterfaceEntity.this))
                    || MEProcessInterfaceEntity.this.returnTankContainer.tickWork(
                            IActionSource.ofMachine(MEProcessInterfaceEntity.this));
        }

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(5, 120, this.getWorkStatus(), true);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int prevTick) {
            if (!MEProcessInterfaceEntity.this.getMainNode().isActive()) return TickRateModulation.SLEEP;
            return this.getWorkStatus()
                    ? (this.doWork() ? TickRateModulation.URGENT : TickRateModulation.SLOWER)
                    : TickRateModulation.SLEEP;
        }
    }
}
