package de.raidcraft.items.crafting.recipes;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Silthus
 */
public class CustomShapedRecipe extends ShapedRecipe {

    private final Map<Character, ItemStack> ingredients = new HashMap<>();
    /**
     * Create a shapeless recipe to craft the specified ItemStack. The constructor merely determines the
     * result and type; to set the actual recipe, you'll need to call the appropriate methods.
     *
     * @param result The item you want the recipe to create.
     *
     * @see org.bukkit.inventory.ShapelessRecipe#addIngredient(org.bukkit.Material)
     * @see org.bukkit.inventory.ShapelessRecipe#addIngredient(org.bukkit.material.MaterialData)
     * @see org.bukkit.inventory.ShapelessRecipe#addIngredient(org.bukkit.Material, int)
     * @see org.bukkit.inventory.ShapelessRecipe#addIngredient(int, org.bukkit.Material)
     * @see org.bukkit.inventory.ShapelessRecipe#addIngredient(int, org.bukkit.material.MaterialData)
     * @see org.bukkit.inventory.ShapelessRecipe#addIngredient(int, org.bukkit.Material, int)
     */
    public CustomShapedRecipe(ItemStack result) {

        super(result);
    }

    public CustomShapedRecipe setIngredient(char key, ItemStack itemStack) {

        super.setIngredient(key, (itemStack == null ? null : itemStack.getData()));
        if (itemStack == null) {
            ingredients.remove(key);
            return this;
        }
        ingredients.put(key, itemStack);
        return this;
    }

    @Override
    public Map<Character, ItemStack> getIngredientMap() {

        return new HashMap<>(ingredients);
    }
}
