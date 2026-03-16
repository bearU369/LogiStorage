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
package com.bear369.logistorage.shared.computercraft.peripherial;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.bear369.logistorage.entity.MEProcessInterfaceEntity;
import com.bear369.logistorage.shared.ae2.ICraftingProcessListener;
import com.bear369.logistorage.shared.ae2.util.IPatternActionListener;
import com.bear369.logistorage.util.FluidStackHandler;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class MEProcessInterfacePeripherialLogic extends AbstractAttachedPeripherial
        implements IPatternActionListener, ICraftingProcessListener {
    private final MEProcessInterfaceEntity host;

    public MEProcessInterfacePeripherialLogic(MEProcessInterfaceEntity host) {
        this.host = host;
    }

    @Override
    public String getType() {
        return ForgeRegistries.BLOCKS
                .getKey(this.host.getBlockState().getBlock())
                .toString();
    }

    @Override
    public Object getTarget() {
        return host;
    }

    @Override
    public void beginPatternSend(GenericStack[] outputs) {
        Objects.requireNonNull(outputs);
        List<Object> insertedList = new ArrayList<>();
        for (GenericStack stack : outputs) {
            final GenericStackRecord record = new GenericStackRecord(stack);
            insertedList.add(record.toTable());
        }
        super.emitEventSignal("crafting_process_preinsert", insertedList);
    }

    @Override
    public void logPatternSend(Map<Integer, GenericStack> insertedSlots) {
        Objects.requireNonNull(insertedSlots);
        final List<Object> insertedList = new ArrayList<>();
        for (int slot : insertedSlots.keySet()) {
            final GenericStackRecord record = new GenericStackRecord(insertedSlots.get(slot));
            insertedList.add(record.toTable(slot + 1)); // +1 for Lua-aligned indexing
        }
        super.emitEventSignal("crafting_process_insert", insertedList);
    }

    @Override
    public void finalizePattenSend(GenericStack[] stacks) {
        Objects.requireNonNull(stacks);
        final List<Object> insertedList = new ArrayList<>();
        for (GenericStack stack : stacks) {
            final GenericStackRecord record = new GenericStackRecord(stack);
            insertedList.add(record.toTable());
        }
        super.emitEventSignal("crafting_process_postinsert", insertedList);
    }

    @Override
    public void onCraftingBegin(GenericStack stack, String triggeringSrc) {
        final GenericStackRecord record = new GenericStackRecord(stack);
        super.emitEventSignal("crafting_start", record.name, record.amount, triggeringSrc);
    }

    @Override
    public void onCraftingFinish(GenericStack stack, String triggeringSrc) {
        final GenericStackRecord record = new GenericStackRecord(stack);
        super.emitEventSignal("crafting_complete", record.name, record.amount, triggeringSrc);
    }

    @Override
    public void onCraftingCancel(GenericStack stack, String triggeringSrc) {
        final GenericStackRecord record = new GenericStackRecord(stack);
        super.emitEventSignal("crafting_cancel", record.name, record.amount, triggeringSrc);
    }

    public IItemHandler getItemHandler() {
        return this.host.getExposedItemInventory().getCapability().orElseThrow(NullPointerException::new);
    }

    public FluidStackHandler getFluidStackHandler() {
        return this.host.getExposedFluidContainer().getCapability().orElseThrow(NullPointerException::new);
    }

    private class GenericStackRecord {
        String name = null;
        long amount = 0;
        boolean isFluid = false;

        public GenericStackRecord(GenericStack stack) {
            Objects.requireNonNull(stack);
            final AEKey key = stack.what();
            this.name = key.getId().toString();
            this.amount = stack.amount();
            this.isFluid = key instanceof AEFluidKey;
        }

        Map<String, Object> toTable(@Nullable Integer slot) {
            final Map<String, Object> table = new Hashtable<>();
            table.put("id", this.name);
            table.put("amount", this.amount);
            table.put("isFluid", this.isFluid);
            if (slot != null) table.put("slot", slot.intValue());
            return table;
        }

        Map<String, Object> toTable() {
            return this.toTable(null);
        }
    }
}
