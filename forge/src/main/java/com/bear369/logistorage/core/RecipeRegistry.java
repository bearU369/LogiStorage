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
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Level;

public class RecipeRegistry extends RecipeProvider {

    public RecipeRegistry(PackOutput packet) {
        super(packet);
    }

    @Override
    protected void buildRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
        Main.LOGGER.log(Level.INFO, "Registering item recipes!");
        if (SharedIntegration.CC_IS_LOADED && SharedIntegration.AE_IS_LOADED) {
            Main.LOGGER.log(Level.INFO, "Registering CC+AE item recipes!");
            final Item AE2_ME_INTERFACE = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("ae2:interface"));
            final Item CC_COMPUTER_NORMAL =
                    ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("computercraft:computer_normal"));
            final Item CC_COMPUTER_ADVANCED =
                    ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("computercraft:computer_advanced"));
            final Item CC_WIRED_MODEM_FULL =
                    ForgeRegistries.ITEMS.getValue(ResourceLocation.parse("computercraft:wired_modem_full"));

            if (AE2_ME_INTERFACE == null
                    || CC_COMPUTER_NORMAL == null
                    || CC_WIRED_MODEM_FULL == null
                    || ItemRegistry.ME_INTERFACE == null) {
                Main.LOGGER.log(Level.WARN, "Unable to register CC-AE item/s recipes due to missing items");
                return;
            }
            if (!ItemRegistry.ME_INTERFACE.isPresent()) {
                Main.LOGGER.log(Level.WARN, "Unable to continue due to me_interface_block is not present");
                return;
            }

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ItemRegistry.ME_INTERFACE.get(), 1)
                    .requires(AE2_ME_INTERFACE)
                    .requires(CC_COMPUTER_NORMAL)
                    .requires(CC_WIRED_MODEM_FULL)
                    .unlockedBy("has_me_interface_block", has(ItemRegistry.ME_INTERFACE.get()))
                    .save(consumer);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ItemRegistry.ME_PROCESSING_INTERFACE.get(), 1)
                    .requires(AE2_ME_INTERFACE)
                    .requires(CC_COMPUTER_ADVANCED)
                    .requires(CC_WIRED_MODEM_FULL)
                    .unlockedBy("has_me_processing_modem_interface", has(ItemRegistry.ME_PROCESSING_INTERFACE.get()))
                    .save(consumer);
        }
    }
}
