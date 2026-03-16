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

import com.bear369.logistorage.block.BlockMEInterface;
import com.bear369.logistorage.block.BlockMEProcessInterface;
import com.bear369.logistorage.shared.SharedIntegration;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MOD_ID);

    public static RegistryObject<Block> ME_INTERFACE = null;
    public static RegistryObject<Block> ME_PROCESSING_INTERFACE = null;

    public static void registerBlocks(IEventBus eventBus) {
        if (SharedIntegration.CC_IS_LOADED && SharedIntegration.AE_IS_LOADED) {
            ME_INTERFACE = BLOCKS.register("me_interface", () -> new BlockMEInterface());
            ME_PROCESSING_INTERFACE = BLOCKS.register("me_processing_interface", () -> new BlockMEProcessInterface());
        }

        BLOCKS.register(eventBus);
    }
}
