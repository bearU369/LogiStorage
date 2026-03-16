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
package com.bear369.logistorage.menu.tab;

import com.bear369.logistorage.menu.gui.ITabProvider;
import com.bear369.logistorage.menu.gui.ItemButton;
import com.bear369.logistorage.shared.AE2;
import com.bear369.logistorage.util.FluidContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;

public class MEProcessInterfaceTabInventory extends GenericTabInventory {

    public MEProcessInterfaceTabInventory(Container itemContainer, FluidContainer fluidContainer) {
        super(itemContainer, fluidContainer);
    }

    @Override
    public void layoutScreen(ITabProvider provider) {
        ItemButton tmpBTN = new ItemButton(
                        provider.getLeftPos() - 20, provider.getTopPos(), 20, 20, AE2.PROCESSING_PATTERN, (button) -> {
                            provider.swapTabFromID(1);
                        })
                .addComponent(Component.translatable("gui.logistorage.me_processing_interface.button.pattern")
                        .withStyle(ChatFormatting.WHITE))
                .addComponent(Component.translatable("gui.logistorage.me_processing_interface.button.pattern.tooltip")
                        .withStyle(ChatFormatting.GRAY));

        provider.addWidget(tmpBTN);
    }
}
