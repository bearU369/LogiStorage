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
package com.bear369.logistorage.shared;

import appeng.api.networking.GridServices;
import appeng.api.stacks.GenericStack;
import com.bear369.logistorage.shared.ae2.services.CraftingEventService;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class AE2 {

    public static Item PROCESSING_PATTERN;
    public static Item EMPTY_PATTERN;

    public static void init() {
        GridServices.register(CraftingEventService.class, CraftingEventService.class);

        PROCESSING_PATTERN = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("ae2:processing_pattern"));
        EMPTY_PATTERN = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("ae2:blank_pattern"));
    }

    public static @Nonnull CompoundTag writeToTag(GenericStack[] stackArr) {
        CompoundTag tag = new CompoundTag();
        if (stackArr == null) return tag;
        ListTag arrayTag = new ListTag();
        for (GenericStack stack : stackArr) {
            arrayTag.add(GenericStack.writeTag(stack));
        }
        tag.put("Array", arrayTag);
        return tag;
    }

    public static @Nullable GenericStack[] readGSArrayFromTag(CompoundTag tag) {
        if (tag == null) return null;
        ListTag arrayTag = tag.contains("Array") ? tag.getList("Array", Tag.TAG_COMPOUND) : null;
        if (arrayTag == null) return null;
        GenericStack[] stack = new GenericStack[arrayTag.size()];
        for (int i = 0; i < arrayTag.size(); i++) {
            stack[i] = GenericStack.readTag(arrayTag.getCompound(i));
        }
        return stack;
    }
}
