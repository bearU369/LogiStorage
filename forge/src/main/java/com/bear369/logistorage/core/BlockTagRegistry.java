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

import com.bear369.logistorage.shared.SharedIntegration;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagRegistry extends BlockTagsProvider {

    public BlockTagRegistry(
            PackOutput output,
            CompletableFuture<Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Main.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull Provider provider) {
        if (SharedIntegration.AE_IS_LOADED && SharedIntegration.CC_IS_LOADED) {
            this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(BlockRegistry.ME_INTERFACE.get())
                    .add(BlockRegistry.ME_PROCESSING_INTERFACE.get());
            this.tag(BlockTags.NEEDS_IRON_TOOL)
                    .add(BlockRegistry.ME_INTERFACE.get())
                    .add(BlockRegistry.ME_PROCESSING_INTERFACE.get());
        }
    }
}
