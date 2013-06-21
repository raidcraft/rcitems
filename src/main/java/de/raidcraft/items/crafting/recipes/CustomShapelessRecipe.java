package de.raidcraft.items.crafting.recipes;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Silthus
 */
public class CustomShapelessRecipe extends ShapelessRecipe {

    private final Map<String, Integer> ingredients = new HashMap<>();
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
    public CustomShapelessRecipe(ItemStack result) {

        super(result);
    }

    public CustomShapelessRecipe addIngredient(int amount, ItemStack ingredient) {

        super.addIngredient(amount, ingredient.getData());
        // lets query for the name of the custom item stack and add it to our list
        String item = RaidCraft.getItemIdString(ingredient);
        Integer previousAmount = ingredients.remove(item);
        if (previousAmount != null) {
            amount += previousAmount;
        }
        // dont allow item amounts greater than nine, because that makes no sense...
        if (amount > 9) amount = 9;
        ingredients.put(item, amount);
        return this;
    }

    public CustomShapelessRecipe removeIngredient(int amount, ItemStack ingredient) {

        super.removeIngredient(amount, ingredient.getData());
        String item = RaidCraft.getItemIdString(ingredient);
        Integer currentAmount = ingredients.remove(item);
        if (currentAmount != null && currentAmount > amount) {
            ingredients.put(item, currentAmount - amount);
        }
        return this;
    }

    public CustomShapelessRecipe removeIngredient(ItemStack ingredient) {

        super.removeIngredient(ingredient.getData());
        String item = RaidCraft.getItemIdString(ingredient);
        ingredients.remove(item);
        return this;
    }

    @Override
    public List<ItemStack> getIngredientList() {

        ArrayList<ItemStack> items = new ArrayList<>();
        for (String id : ingredients.keySet()) {
            try {
                ItemStack item = RaidCraft.getItem(id);
                item.setAmount(ingredients.get(id));
                items.add(item);
            } catch (CustomItemException e) {
                RaidCraft.LOGGER.warning(e.getMessage());
            }
        }
        return items;
    }
}
