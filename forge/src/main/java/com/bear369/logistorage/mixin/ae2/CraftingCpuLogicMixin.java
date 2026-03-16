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

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.bear369.logistorage.entity.MEProcessInterfaceEntity;
import com.bear369.logistorage.shared.ae2.ICraftingCpuLogic;
import com.bear369.logistorage.shared.ae2.containers.PatternContainer;
import com.bear369.logistorage.shared.ae2.services.CraftingEventService;
import com.bear369.logistorage.shared.ae2.util.CraftingPatternTracker;
import com.bear369.logistorage.shared.ae2.util.CraftingSignalEvent;
import com.bear369.logistorage.shared.computercraft.peripherial.MEProcessInterfacePeripherialLogic;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "appeng.crafting.execution.CraftingCpuLogic", remap = false)
@Pseudo
public abstract class CraftingCpuLogicMixin implements ICraftingCpuLogic {
    @Unique
    private String triggeringSource = null;

    @Unique
    private Set<ICraftingProvider> activeProvider = new HashSet<>();

    @Unique
    private UUID activeCraftingID = null;

    @Shadow
    protected CraftingCPUCluster cluster;

    @Shadow
    protected ExecutingCraftingJob job;

    @WrapOperation(
            method = "trySubmitJob",
            at = @At(value = "NEW", target = "appeng/crafting/CraftingLink", ordinal = 0),
            remap = false)
    private CraftingLink onSubmitJob(
            CompoundTag data,
            ICraftingCPU cpu,
            Operation<CraftingLink> org,
            @Local(argsOnly = true) IGrid grid,
            @Local(argsOnly = true) ICraftingPlan plan,
            @Local(argsOnly = true) IActionSource src,
            @Local(argsOnly = true) ICraftingRequester requester,
            @Local UUID craftId) {
        CraftingEventService eventService = grid.getService(CraftingEventService.class);
        this.triggeringSource = requester != null
                ? CraftingEventService.getTriggeringNameFromSource(IActionSource.ofMachine(requester))
                : CraftingEventService.getTriggeringNameFromSource(src);
        this.activeCraftingID = craftId;
        eventService.invokeCraftingEvent(plan.finalOutput(), this.triggeringSource, CraftingSignalEvent.CRAFTING_START);
        return org.call(data, cpu);
    }

    @Inject(method = "finishJob", remap = false, at = @At("HEAD"))
    private void onFinishJob(boolean success, CallbackInfo ci) {
        CraftingEventService eventService = this.cluster.getGrid().getService(CraftingEventService.class);
        for (ICraftingProvider provider : activeProvider) {
            if (provider instanceof PatternContainer patternContainer
                    && patternContainer.getHost() instanceof MEProcessInterfaceEntity entity) {
                final CraftingPatternTracker tracker = patternContainer.getCraftingTracker();
                final MEProcessInterfacePeripherialLogic peripheral =
                        (MEProcessInterfacePeripherialLogic) entity.getPeripheral();
                peripheral.finalizePattenSend(tracker.getOutputsById(this.activeCraftingID));
                tracker.removeCraftingID(this.activeCraftingID);
                String itemID = getFinalJobOutput().what().getId().toString();
                long targetAmount = getFinalJobOutput().amount();

                peripheral.emitEventSignal(
                        success ? "crafting_process_complete" : "crafting_process_cancel",
                        itemID,
                        targetAmount,
                        this.triggeringSource);
            }
        }
        eventService.invokeCraftingEvent(
                this.getFinalJobOutput(),
                this.triggeringSource,
                success ? CraftingSignalEvent.CRAFTING_COMPLETE : CraftingSignalEvent.CRAFTING_CANCEL);

        activeProvider.clear();
        this.activeCraftingID = null;
        this.triggeringSource = null;
    }

    @WrapOperation(
            method = "executeCrafting",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lappeng/api/networking/crafting/ICraftingProvider;pushPattern(Lappeng/api/crafting/IPatternDetails;[Lappeng/api/stacks/KeyCounter;)Z"),
            remap = false)
    private boolean onPushPattern(
            ICraftingProvider provider,
            IPatternDetails details,
            KeyCounter[] craftingContainer,
            Operation<Boolean> org) {
        if (provider instanceof PatternContainer patternProvider
                && patternProvider.getHost() instanceof MEProcessInterfaceEntity entity) {
            if (!patternProvider.shouldPushPattern(details, craftingContainer)) return false;
            final MEProcessInterfacePeripherialLogic peripheral =
                    (MEProcessInterfacePeripherialLogic) entity.getPeripheral();
            final CraftingPatternTracker tracker = patternProvider.getCraftingTracker();
            if (activeProvider.contains(patternProvider)) {
                if (!tracker.compareOutputs(this.activeCraftingID, details.getOutputs())) {
                    peripheral.finalizePattenSend(tracker.getOutputsById(this.activeCraftingID));
                    peripheral.beginPatternSend(details.getOutputs());
                    tracker.setOutputs(this.activeCraftingID, details.getOutputs());
                }
            } else {
                String itemID = getFinalJobOutput().what().getId().toString();
                long targetAmount = getFinalJobOutput().amount();
                peripheral.emitEventSignal("crafting_process_start", itemID, targetAmount, triggeringSource);

                activeProvider.add(patternProvider);
                peripheral.beginPatternSend(details.getOutputs());
                tracker.addCraftingID(this.activeCraftingID);
                tracker.setOutputs(this.activeCraftingID, details.getOutputs());
            }
        }
        return org.call(provider, details, craftingContainer);
    }

    @Override
    public boolean refreshAddActiveProvider(ICraftingProvider provider) {
        if (provider instanceof PatternContainer patternContainer
                && patternContainer.getCraftingTracker().containsCraftingID(activeCraftingID)) {
            return this.activeProvider.add(provider);
        }
        return false;
    }

    @Inject(method = "readFromNBT", remap = false, at = @At("TAIL"))
    private void onReadFromNBT(CompoundTag data, CallbackInfo ci) {
        if (data.contains("triggeringSource")) this.triggeringSource = data.getString("triggeringSource");
        if (data.contains("activeCraftingID")) this.activeCraftingID = data.getUUID("activeCraftingID");
    }

    @Inject(method = "writeToNBT", remap = false, at = @At("TAIL"))
    private void onWriteToNBT(CompoundTag data, CallbackInfo ci) {
        if (this.triggeringSource != null) data.putString("triggeringSource", this.triggeringSource);
        if (this.activeCraftingID != null) data.putUUID("activeCraftingID", this.activeCraftingID);
    }

    @Shadow(aliases = "hasJob", remap = false)
    public abstract boolean hasJob();

    @Shadow(aliases = "getFinalJobOutput", remap = false)
    public abstract GenericStack getFinalJobOutput();
}
