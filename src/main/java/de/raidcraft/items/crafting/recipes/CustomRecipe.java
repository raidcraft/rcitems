package de.raidcraft.items.crafting.recipes;

import de.raidcraft.items.crafting.CraftingRecipeType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Recipe;

/**
 * @author Silthus
 */
public interface CustomRecipe extends Recipe {

    public String getName();

    public CraftingRecipeType getType();

    public String getPermission();

    public boolean isMatchingRecipe(CraftingInventory inventory);

    public void save();
}
