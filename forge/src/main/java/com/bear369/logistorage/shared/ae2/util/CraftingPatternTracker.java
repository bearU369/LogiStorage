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
package com.bear369.logistorage.shared.ae2.util;

import appeng.api.stacks.GenericStack;
import com.bear369.logistorage.shared.AE2;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

public class CraftingPatternTracker {
    private final Set<UUID> trackedCraftingIDs = new HashSet<>();
    private final Map<UUID, GenericStack[]> trackedOutputs = new HashMap<>();

    public @Nonnull CompoundTag serializeNBT() {
        final CompoundTag trackerTag = new CompoundTag();
        trackerTag.put("Outputs", this.serializeMapOutputsNBT());
        trackerTag.put("IDs", this.serializeCraftingIDsNBT());
        return trackerTag;
    }

    public void deserializeNBT(CompoundTag tag) {
        final CompoundTag outputTag = tag.contains("Outputs") ? tag.getCompound("Outputs") : new CompoundTag();
        final CompoundTag idTag = tag.contains("IDs") ? tag.getCompound("IDs") : new CompoundTag();
        this.deserializeCraftingIDsNBT(idTag);
        this.deserializeMapOutputsNBT(outputTag);
    }

    public void clearCraftingIDs() {
        this.trackedCraftingIDs.clear();
        this.trackedOutputs.clear();
    }

    public boolean addCraftingID(UUID id) {
        return trackedCraftingIDs.add(id);
    }

    public boolean removeCraftingID(UUID id) {
        final boolean isRemoved = trackedCraftingIDs.remove(id);
        if (isRemoved) trackedOutputs.remove(id);
        return isRemoved;
    }

    public boolean containsCraftingID(UUID id) {
        return trackedCraftingIDs.contains(id);
    }

    public boolean isCraftingIDsEmpty() {
        return trackedCraftingIDs.isEmpty();
    }

    public void setOutputs(UUID targetID, GenericStack[] outputs) {
        if (!this.trackedCraftingIDs.contains(targetID)) return;
        this.trackedOutputs.put(targetID, outputs);
    }

    public GenericStack[] getOutputsById(UUID id) {
        return this.trackedOutputs.get(id);
    }

    public boolean compareOutputs(UUID targetID, GenericStack[] outputs) {
        if (targetID == null || trackedOutputs == null || outputs == null) return false;
        final GenericStack[] targetOutput = trackedOutputs.get(targetID);
        if (targetOutput == null) return false;
        if (targetOutput.length != outputs.length) return false;
        for (int i = 0; i < targetOutput.length; i++) {
            if (!targetOutput[i].equals(outputs[i])) return false;
        }
        return true;
    }

    private @Nonnull CompoundTag serializeCraftingIDsNBT() {
        if (this.trackedCraftingIDs.isEmpty()) return new CompoundTag();
        final CompoundTag coreTag = new CompoundTag();
        final ListTag arrayTag = new ListTag();
        for (UUID id : this.trackedCraftingIDs) {
            arrayTag.add(NbtUtils.createUUID(id));
        }
        coreTag.put("array", arrayTag);
        return coreTag;
    }

    private @Nonnull CompoundTag serializeMapOutputsNBT() {
        final CompoundTag compoundTag = new CompoundTag();
        for (UUID id : this.trackedCraftingIDs) {
            final GenericStack[] stack = this.trackedOutputs.get(id);
            if (stack == null) continue;
            compoundTag.put(id.toString(), AE2.writeToTag(stack));
        }
        return compoundTag;
    }

    private void deserializeCraftingIDsNBT(CompoundTag tag) {
        final ListTag arrayTag = tag.contains("array") ? tag.getList("array", Tag.TAG_INT_ARRAY) : null;
        if (arrayTag == null) return;
        for (int i = 0; i < arrayTag.size(); i++) {
            this.trackedCraftingIDs.add(NbtUtils.loadUUID(arrayTag.get(i)));
        }
    }

    private void deserializeMapOutputsNBT(CompoundTag tag) {
        final Set<String> keys = tag.getAllKeys();
        if (keys.size() != this.trackedCraftingIDs.size()) return;
        for (String key : keys) {
            final UUID tmpID = UUID.fromString(key);
            if (!this.trackedCraftingIDs.contains(tmpID)) continue;
            final GenericStack[] stack = AE2.readGSArrayFromTag(tag.getCompound(key));
            this.trackedOutputs.put(tmpID, stack);
        }
    }
}
