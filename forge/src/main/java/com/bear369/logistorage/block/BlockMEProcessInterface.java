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
package com.bear369.logistorage.block;

import com.bear369.logistorage.entity.MEProcessInterfaceEntity;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMEProcessInterface extends AbstractAEBlockEntity {

    @Override
    public void onRemove(
            @Nonnull BlockState state,
            @Nonnull Level level,
            @Nonnull BlockPos pos,
            @Nonnull BlockState newState,
            boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            final BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof MEProcessInterfaceEntity curEnt) {
                Containers.dropContents(level, pos, curEnt.getExposedItemInventory());
                Containers.dropContents(level, pos, curEnt.getExposedReturnInventory());
                Containers.dropContents(level, pos, curEnt.getExposedPatternContainer());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new MEProcessInterfaceEntity(pos, state);
    }
}
