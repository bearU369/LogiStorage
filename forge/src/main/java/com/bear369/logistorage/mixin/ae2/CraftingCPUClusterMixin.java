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
package com.bear369.logistorage.mixin.ae2;

import appeng.crafting.execution.CraftingCpuLogic;
import com.bear369.logistorage.shared.ae2.ICraftingCPUCluster;
import com.bear369.logistorage.shared.ae2.ICraftingCpuLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "appeng.me.cluster.implementations.CraftingCPUCluster", remap = false)
public abstract class CraftingCPUClusterMixin implements ICraftingCPUCluster {

    @Shadow
    public CraftingCpuLogic craftingLogic;

    @Override
    public ICraftingCpuLogic getCraftingCpuLogic() {
        return (ICraftingCpuLogic) craftingLogic;
    }
}
