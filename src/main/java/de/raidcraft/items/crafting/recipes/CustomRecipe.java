package de.raidcraft.items.crafting.recipes;

import de.raidcraft.items.crafting.CraftingRecipeType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Recipe;

/**
 * @author Silthus
 */
public interface CustomRecipe extends Recipe {

    String getName();

    CraftingRecipeType getType();

    String getPermission();

    boolean isMatchingRecipe(CraftingInventory inventory);

    void save();
}
