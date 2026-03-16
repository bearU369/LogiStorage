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
package com.bear369.logistorage.menu.screen;

import com.bear369.logistorage.menu.AbstractTabContainerMenu;
import com.bear369.logistorage.menu.gui.FluidSlot;
import com.bear369.logistorage.menu.gui.ITabProvider;
import com.bear369.logistorage.menu.tab.AbstractTabContainer;
import com.bear369.logistorage.network.NetworkHandler;
import com.bear369.logistorage.network.ServerTabContainerPacket;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AbstractTabContainerScreen<T extends AbstractTabContainerMenu> extends AbstractMixContainerScreen<T>
        implements ITabProvider {

    public AbstractTabContainerScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.resizeUI();
        this.getCurrentTab().layoutScreen(this);
    }

    public void refreshTab() {
        this.clearWidgets();
        this.resizeUI();
        this.getCurrentTab().layoutScreen(this);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.getCurrentTab().render(guiGraphics, partialTick, mouseX, mouseY, this);
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderDisabled(guiGraphics);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void resizeUI() {
        final AbstractTabContainer activeTab = this.getCurrentTab();
        this.imageWidth = activeTab.imageWidth;
        this.imageHeight = activeTab.imageHeight;
        this.inventoryLabelY = activeTab.inventoryLabelY;

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public void onHoverRender(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.onHoverRender(guiGraphics, mouseX, mouseY);
    }

    private AbstractTabContainer getCurrentTab() {
        if (menu.getActiveTabContainer() == null) throw new NullPointerException("AbstractTabContainer is missing");
        return menu.getActiveTabContainer();
    }

    // === ITabProvider === //
    @Override
    public void swapTabFromID(int index) {
        NetworkHandler.sendToServer(new ServerTabContainerPacket(this.menu.containerId, index));
    }

    @Override
    public <U extends AbstractWidget> void addWidget(U widget) {
        if (widget == null) return;
        this.addRenderableWidget(widget);
    }

    @Override
    public List<FluidSlot> getFluidSlots() {
        return this.menu.fluidSlots;
    }

    @Override
    public int getLeftPos() {
        return this.leftPos;
    }

    @Override
    public int getTopPos() {
        return this.topPos;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
    // === ITabProvider === //
}
