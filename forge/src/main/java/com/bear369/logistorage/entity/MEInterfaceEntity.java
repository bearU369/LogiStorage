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
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import com.bear369.logistorage.core.BlockEntityTypeRegistry;
import com.bear369.logistorage.core.Main;
import com.bear369.logistorage.menu.AbstractTabContainerMenu;
import com.bear369.logistorage.menu.GenericTabContainerMenu;
import com.bear369.logistorage.menu.tab.AbstractTabContainer;
import com.bear369.logistorage.menu.tab.GenericTabInventory;
import com.bear369.logistorage.shared.ComputerCraft;
import com.bear369.logistorage.shared.ae2.containers.ReturnFluidInvContainer;
import com.bear369.logistorage.shared.ae2.containers.ReturnItemInvContainer;
import com.bear369.logistorage.shared.command.MEFluidCommand;
import com.bear369.logistorage.shared.command.MEItemCommand;
import com.bear369.logistorage.shared.command.MENetworkCommand;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;

public class MEInterfaceEntity extends AENetworkBlockEntity implements IActionHost, MenuProvider {
    private final ReturnItemInvContainer returnInv = new ReturnItemInvContainer(9, this);
    private final ReturnFluidInvContainer returnTank = new ReturnFluidInvContainer(1, this);
    private final CCAbility abilityCC = new CCAbility();

    private final LazyOptional<IPeripheral> peripherialCap = LazyOptional.of(() -> abilityCC);

    public MEInterfaceEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityTypeRegistry.ME_INTERFACE.get(), blockPos, blockState);
        this.addService(IGridTickable.class, new Ticker());
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ComputerCraft.CAPABILITY_PERIPHERIAL) {
            return peripherialCap.cast();
        } else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return returnInv.getCapability().cast();
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return returnTank.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        peripherialCap.invalidate();
        returnInv.getCapability().invalidate();
        returnTank.getCapability().invalidate();
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("ReturnItemInvContainer", this.returnInv.serializeNBT());
        tag.put("ReturnFluidInvContainer", this.returnTank.serializeNBT());
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("ReturnItemInvContainer"))
            this.returnInv.deserializeNBT(tag.getCompound("ReturnItemInvContainer"));
        if (tag.contains("ReturnFluidInvContainer"))
            this.returnTank.deserializeNBT(tag.getCompound("ReturnFluidInvContainer"));
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) {
        final AbstractTabContainerMenu menu = new GenericTabContainerMenu(id, inventory, this);
        menu.registerTabs(this.getTabContainers());
        return menu;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.logistorage.me_interface");
    }

    private List<AbstractTabContainer> getTabContainers() {
        final List<AbstractTabContainer> list = new ArrayList<>();
        list.add(new GenericTabInventory(returnInv, returnTank));
        return list;
    }

    @Override
    protected double getIdlePowerUsage() {
        return 8.0;
    }

    @Override
    public IGridNode getActionableNode() {
        return super.getMainNode();
    }

    public Container getItemContainer() {
        return this.returnInv;
    }

    private class CCAbility implements IDynamicPeripheral {
        public Hashtable<Integer, IComputerAccess> computerTable = new Hashtable<>();

        @Override
        public void attach(IComputerAccess computer) {
            Main.LOGGER.log(Level.INFO, String.format("Entity attached to computer #%d!", computer.getID()));
            computerTable.put(computer.getID(), computer);
        }

        @Override
        public void detach(IComputerAccess computer) {
            Main.LOGGER.log(Level.INFO, String.format("Entity detached from computer #%d!", computer.getID()));
            computerTable.remove(computer.getID());
        }

        @Override
        public String[] getMethodNames() {
            return new String[] {
                "getNetworkInfo",
                "listItems",
                "listFluids",
                "findItem",
                "findFluid",
                "pullItem",
                "pullFluid",
                "pushItem",
                "pushFluid",
            };
        }

        @Override
        public MethodResult callMethod(IComputerAccess computer, ILuaContext context, int method, IArguments arguments)
                throws LuaException {
            switch (method) {
                case 0:
                    return MENetworkCommand.getNetworkInfo(computer, context, arguments, getMainNode());
                case 1:
                    return MEItemCommand.listItems(computer, context, arguments, MEInterfaceEntity.this);
                case 2:
                    return MEFluidCommand.listFluids(computer, context, arguments, MEInterfaceEntity.this);
                case 3:
                    return MEItemCommand.findItem(computer, context, arguments, MEInterfaceEntity.this);
                case 4:
                    return MEFluidCommand.findFluid(computer, context, arguments, MEInterfaceEntity.this);
                case 5:
                    return MEItemCommand.pullItem(computer, context, arguments, MEInterfaceEntity.this, this);
                case 6:
                    return MEFluidCommand.pullFluid(computer, context, arguments, MEInterfaceEntity.this, this);
                case 7:
                    return MEItemCommand.pushItem(computer, context, arguments, MEInterfaceEntity.this, this);
                case 8:
                    return MEFluidCommand.pushFluid(computer, context, arguments, MEInterfaceEntity.this, this);
                default:
                    throw new LuaException("Invalid method index");
            }
        }

        @Override
        public boolean equals(IPeripheral other) {
            return this == other;
        }

        @Override
        public String getType() {
            return ForgeRegistries.BLOCKS
                    .getKey(MEInterfaceEntity.this.getBlockState().getBlock())
                    .toString();
        }

        @Override
        public Set<String> getAdditionalTypes() {
            return Set.of("inventory", "fluid_storage");
        }

        @Override
        public Object getTarget() {
            return MEInterfaceEntity.this;
        }
    }

    private class Ticker implements IGridTickable {
        boolean getWorkStatus() {
            return !MEInterfaceEntity.this.returnInv.isEmpty() || !MEInterfaceEntity.this.returnTank.isEmpty();
        }

        boolean doWork() {
            final IActionSource src = IActionSource.ofMachine(MEInterfaceEntity.this);
            return MEInterfaceEntity.this.returnInv.tickWork(src) || MEInterfaceEntity.this.returnTank.tickWork(src);
        }

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(5, 120, this.getWorkStatus(), true);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!MEInterfaceEntity.this.getMainNode().isActive()) return TickRateModulation.SLEEP;
            return this.getWorkStatus()
                    ? (this.doWork() ? TickRateModulation.URGENT : TickRateModulation.SLOWER)
                    : TickRateModulation.SLEEP;
        }
    }
}
