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
package com.bear369.logistorage.util;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public final class GUIRenderUtil {

    protected GUIRenderUtil() {}

    public static void renderFluid(
            GuiGraphics guiGraphics,
            FluidStack fluidStack,
            int x,
            int y,
            int width,
            int height,
            int capacity,
            boolean isScissor) {
        if (fluidStack.isEmpty()) return;

        final IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        final ResourceLocation texture = props.getStillTexture();
        final int color = props.getTintColor();

        final ResourceLocation BLOCK_ATLAS = InventoryMenu.BLOCK_ATLAS;
        Objects.requireNonNull(BLOCK_ATLAS);

        final TextureAtlasSprite sprite =
                Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS).apply(texture);
        Objects.requireNonNull(sprite);

        final int fluidHeight = (int) (height * ((float) fluidStack.getAmount() / capacity));
        if (fluidHeight < 1) return;
        final float a = ((color >> 24) & 0xFF) / 255f;
        final float r = ((color >> 16) & 0xFF) / 255f;
        final float g = ((color >> 8) & 0xFF) / 255f;
        final float b = (color & 0xFF) / 255f;

        if (isScissor) guiGraphics.enableScissor(x, y, x + width, y + height);

        guiGraphics.setColor(r, g, b, a);
        guiGraphics.blit(x, y + (height - fluidHeight), 0, width, height, sprite);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        if (isScissor) guiGraphics.disableScissor();
    }

    public static void renderFluid(
            GuiGraphics guiGraphics, FluidStack fluidStack, int x, int y, int width, int height, int capacity) {
        GUIRenderUtil.renderFluid(guiGraphics, fluidStack, x, y, width, height, capacity, true);
    }
}
