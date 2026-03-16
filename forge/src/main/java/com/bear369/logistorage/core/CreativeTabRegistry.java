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
package com.bear369.logistorage.core;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class CreativeTabRegistry {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MOD_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MOD_ID);

    public static final Supplier<CreativeModeTab> CUSTOM_TAB = CREATIVE_MOD_TABS.register(
            "creative_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.%s.creative_tab".formatted(Main.MOD_ID)))
                    .icon(() -> {
                        if (ItemRegistry.ME_INTERFACE != null) return new ItemStack(ItemRegistry.ME_INTERFACE.get());
                        return Items.CHEST.getDefaultInstance();
                    })
                    .displayItems((parameters, output) -> {
                        if (ItemRegistry.ME_INTERFACE != null) output.accept(ItemRegistry.ME_INTERFACE.get());
                        if (ItemRegistry.ME_PROCESSING_INTERFACE != null)
                            output.accept(ItemRegistry.ME_PROCESSING_INTERFACE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MOD_TABS.register(eventBus);
    }
}
