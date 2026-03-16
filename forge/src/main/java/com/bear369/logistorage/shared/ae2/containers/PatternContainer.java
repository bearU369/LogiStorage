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

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import com.bear369.logistorage.core.Main;
import com.bear369.logistorage.entity.AENetworkBlockEntity;
import com.bear369.logistorage.shared.ae2.ICraftingCPUCluster;
import com.bear369.logistorage.shared.ae2.util.CraftingPatternTracker;
import com.bear369.logistorage.shared.ae2.util.IPatternActionListener;
import com.bear369.logistorage.util.FluidContainer;
import com.bear369.logistorage.util.FluidStackHandler;
import com.bear369.logistorage.util.ItemContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;

public class PatternContainer extends ItemContainer implements ICraftingProvider {
    private final AENetworkBlockEntity host;
    private final List<IPatternDetails> patterns = new ArrayList<>();
    private final List<GenericStack> sendList = new ArrayList<>();

    private final Set<IPatternActionListener> listeners = new HashSet<>();
    private final Map<Integer, GenericStack> insertedSlots = new HashMap<>();

    private final ItemContainer itemContainer;
    private final FluidContainer fluidContainer;

    private final CraftingPatternTracker craftingTracker = new CraftingPatternTracker();
    private boolean isInitialized = false;

    public PatternContainer(
            AENetworkBlockEntity host,
            int size,
            ItemContainer targetItemContainer,
            FluidContainer targetFluidContainer) {
        super(size);
        this.host = host;
        this.itemContainer = targetItemContainer;
        this.fluidContainer = targetFluidContainer;
    }

    @Override
    public @Nonnull CompoundTag serializeNBT() {
        final CompoundTag parentNBT = super.serializeNBT();
        final ListTag sendListNBT = new ListTag();
        for (int i = 0; i < this.sendList.size(); i++) {
            final CompoundTag sendStack = new CompoundTag();
            final CompoundTag stackTag = GenericStack.writeTag(this.sendList.get(i));
            if (stackTag == null) continue;
            sendStack.putInt("Slot", i);
            sendStack.put("GenericStack", stackTag);
            sendListNBT.add(sendStack);
        }
        final CompoundTag trackerNBT = this.craftingTracker.serializeNBT();
        parentNBT.put("Tracker", trackerNBT);
        parentNBT.put("SendList", sendListNBT);
        parentNBT.putInt("SendListSize", this.sendList.size());
        return parentNBT;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        final int SLSize = tag.contains("SendListSize") ? tag.getInt("SendListSize") : 0;
        for (int i = 0; i < SLSize; i++) this.sendList.add(GenericStack.fromItemStack(ItemStack.EMPTY));
        final ListTag sendListNBT =
                tag.contains("SendList") ? tag.getList("SendList", ListTag.TAG_COMPOUND) : new ListTag();
        for (int i = 0; i < SLSize; i++) {
            final CompoundTag sendStack = sendListNBT.getCompound(i);
            final int slot = sendStack.getInt("Slot");
            final GenericStack stack = GenericStack.readTag(sendStack.getCompound("GenericStack"));
            this.sendList.set(slot, stack);
        }
        final CompoundTag trackerNBT = tag.contains("Tracker") ? tag.getCompound("Tracker") : null;
        if (trackerNBT != null) this.craftingTracker.deserializeNBT(trackerNBT);
    }

    @Override
    public void setChanged() {
        patterns.clear();
        updatePatterns();
    }

    public void refreshState() {
        updatePatterns();
        if (!this.sendList.isEmpty() || !this.isInitialized)
            this.host.ifPresent((grid, node) -> {
                grid.getTickManager().alertDevice(node);
            });
    }

    // === ICraftingProvider ===//
    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return this.patterns;
    }

    /**
     *  TODO: Add blocking and checking as followed in {@link appeng.helpers.patternprovider.PatternProviderLogic#pushPattern(IPatternDetails, KeyCounter[])}
     */
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!this.shouldPushPattern(patternDetails, inputHolder)) return false;
        patternDetails.pushInputsToExternalInventory(inputHolder, (key, amount) -> {
            this.pushArranged(key, amount, true);
        });
        logSendAction();
        this.sendStacksOut();
        return true;
    }

    public boolean shouldPushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return this.sendList.isEmpty()
                && this.host.getGridNode(null).isActive()
                && this.patterns.contains(patternDetails);
    }

    @Override
    public boolean isBusy() {
        return !this.sendList.isEmpty();
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }
    // === ICraftingProvider ===//

    public AENetworkBlockEntity getHost() {
        return this.host;
    }

    public CraftingPatternTracker getCraftingTracker() {
        return this.craftingTracker;
    }

    /**
     * Used for {@link appeng.api.networking.ticking.IGridTickable IGridTickable}
     */
    public boolean tickWork() {
        final boolean res = this.sendStacksOut();
        logSendAction();
        return res;
    }

    public boolean initCraftingCPU() {
        if (this.isInitialized) return false;
        if (!this.craftingTracker.isCraftingIDsEmpty()) {
            final ICraftingService craftingService =
                    this.host.getGridNode(null).getGrid().getCraftingService();
            var cpus = craftingService.getCpus();
            if (cpus.size() <= 0) return true;

            boolean isDangling = true;
            for (var cpu : cpus) {
                if (cpu instanceof ICraftingCPUCluster cluster
                        && cluster.getCraftingCpuLogic().refreshAddActiveProvider(this)) isDangling = false;
            }
            if (isDangling) { // Delete dangling craftingIDs if found no related ICraftingCPUs.
                Main.LOGGER.warn(
                        "CraftingPatternTracker tracks CraftingIDs but found no attached ICraftingCPULogics, remove dangling IDs");
                this.craftingTracker.clearCraftingIDs();
            }
        }
        this.isInitialized = true;
        return true;
    }

    public boolean addListener(IPatternActionListener listener) {
        return this.listeners.add(listener);
    }

    public boolean removeListener(IPatternActionListener listener) {
        return this.listeners.remove(listener);
    }

    private long pushArranged(AEKey key, long amount, boolean shouldAlert) {
        if (key instanceof AEItemKey item) {
            final IItemHandler cap = itemContainer.getCapability().orElseThrow(NullPointerException::new);
            ItemStack stack = item.toStack((int) amount);
            for (int i = 0; i < itemContainer.getContainerSize(); i++) {
                final int preInsertCount = stack.getCount();
                stack = cap.insertItem(i, stack, false);
                if (preInsertCount != stack.getCount())
                    insertedSlots.put(i, new GenericStack(item, preInsertCount - stack.getCount()));
                if (stack.isEmpty()) break;
            }

            if (!stack.isEmpty() && shouldAlert) perservedSend(key, stack.getCount());
            return stack.getCount();
        } else if (key instanceof AEFluidKey fluid) {
            final FluidStackHandler cap = fluidContainer.getCapability().orElseThrow(NullPointerException::new);
            FluidStack stack = fluid.toStack((int) amount);
            for (int i = 0; i < fluidContainer.getTanks(); i++) {
                int insertedVal = cap.fill(i, stack, FluidAction.EXECUTE);
                stack.shrink(insertedVal);
                if (insertedVal > 0) insertedSlots.put(i, new GenericStack(fluid, insertedVal));
                if (stack.isEmpty()) break;
            }

            if (!stack.isEmpty() && shouldAlert) perservedSend(key, stack.getAmount());
            return stack.getAmount();
        }
        return 0;
    }

    private void logSendAction() {
        if (this.insertedSlots.isEmpty()) return;

        for (IPatternActionListener listener : this.listeners) {
            listener.logPatternSend(this.insertedSlots);
        }
        this.insertedSlots.clear();
    }

    private void perservedSend(AEKey key, long amount) {
        if (amount <= 0) return;
        this.sendList.add(new GenericStack(key, amount));
        this.host.ifPresent((grid, node) -> {
            grid.getTickManager().alertDevice(node);
        });
    }

    private boolean sendStacksOut() {
        if (this.sendList.isEmpty()) return false;
        boolean actionInit = false;
        ListIterator<GenericStack> sendIt = this.sendList.listIterator();
        while (sendIt.hasNext()) {
            final GenericStack stack = sendIt.next();
            final AEKey key = stack.what();
            final long amount = stack.amount();
            long remaining = this.pushArranged(key, amount, false);
            if (remaining <= 0) {
                sendIt.remove();
                actionInit = true;
            } else {
                sendIt.set(new GenericStack(key, remaining));
                actionInit = true;
            }
        }
        return actionInit;
    }

    private void updatePatterns() {
        for (int i = 0; i < this.getContainerSize(); i++) {
            IPatternDetails details = PatternDetailsHelper.decodePattern(super.getItem(i), host.getLevel());

            if (details != null) {
                patterns.add(details);
            }
        }
        this.host.ifPresent((grid, node) -> {
            grid.getCraftingService().refreshNodeCraftingProvider(node);
        });
    }
}
