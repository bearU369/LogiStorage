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

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridNodeListener.State;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This is the mod's simplified implementation of AE2's network blocks, built through purely AE2's APIs for simple connection with AE2 networks.
 * <p>
 * These implementations are built due to AE2's base implementations of networked blocks aren't exposed in their APIs.
 * It is written so without fully importing AE2's runtime library, maintaining AE2 as a soft dependency of this mod.
 * <p>
 * Not to be confused with {@link appeng.blockentity.grid.AENetworkBlockEntity}.
 */
public class AENetworkBlockEntity extends BlockEntity implements IInWorldGridNodeHost {
    private final IManagedGridNode mainNode = GridHelper.createManagedNode(this, AEGridNodeListener.INSTANCE)
            .setInWorldNode(true)
            .setExposedOnSides(getGridConnectionSides())
            .setFlags(getGridFlags().toArray(GridFlags[]::new))
            .setIdlePowerUsage(getIdlePowerUsage());

    public AENetworkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void setRemoved() {
        mainNode.destroy();
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        GridHelper.onFirstTick(this, AENetworkBlockEntity::onFirstTick);
    }

    @Override
    public void onChunkUnloaded() {
        mainNode.destroy();
        super.onChunkUnloaded();
    }

    public boolean ifPresent(BiConsumer<IGrid, IGridNode> action) {
        return mainNode.ifPresent(action);
    }

    @Override
    public IGridNode getGridNode(Direction dir) {
        return mainNode.getNode();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    protected Set<Direction> getGridConnectionSides() {
        return EnumSet.allOf(Direction.class);
    }

    protected Set<GridFlags> getGridFlags() {
        return Set.of(GridFlags.REQUIRE_CHANNEL);
    }

    protected double getIdlePowerUsage() {
        return 2.0;
    }

    protected boolean isGridBooted() {
        return mainNode.hasGridBooted();
    }

    protected IGridNode getMainNode() {
        return mainNode.getNode();
    }

    protected final <T extends IGridNodeService> void addService(Class<T> serviceClass, T nodeOwner) {
        this.mainNode.addService(serviceClass, nodeOwner);
    }

    protected void onFirstTick() {
        mainNode.create(this.getLevel(), this.getBlockPos());
    }

    protected void onStateChanged(IGridNode node, State state) {}

    /**
     * Basic implementation of {@link appeng.me.helpers.BlockEntityNodeListener} for custom AE2 blocks within this mod.
     * <p>
     * @implNote Implementation is isolated within {@link AENetworkBlockEntity} and called on {@link AENetworkBlockEntity#mainNode mainNode} for simplicity and encapsulation. Further changes may occur.
     */
    private static class AEGridNodeListener<T extends AENetworkBlockEntity> implements IGridNodeListener<T> {
        public static final AEGridNodeListener<AENetworkBlockEntity> INSTANCE = new AEGridNodeListener<>();

        @Override
        public void onSaveChanges(T nodeOwner, IGridNode node) {}

        @Override
        public void onStateChanged(T nodeOwner, IGridNode node, State state) {
            nodeOwner.onStateChanged(node, state);
        }
    }
}
