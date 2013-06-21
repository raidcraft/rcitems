package de.raidcraft.items.crafting;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.Component;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.items.tables.TCraftingRecipe;
import de.raidcraft.items.tables.TCraftingRecipeIngredient;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.List;

/**
 * @author Silthus
 */
public class CraftingManager implements Component {

    private final ItemsPlugin plugin;

    public CraftingManager(ItemsPlugin plugin) {

        this.plugin = plugin;
        loadRecipes();
    }

    public void reload() {

        Bukkit.clearRecipes();
        loadRecipes();
    }

    private void loadRecipes() {

        int loadedRecipes = 0;
        Recipe recipe = null;
        ItemStack result;
        List<TCraftingRecipe> recipes = plugin.getDatabase().find(TCraftingRecipe.class).findList();
        for (TCraftingRecipe craftingRecipe : recipes) {
            try {
                // lets parse the result item id string
                result = RaidCraft.getItem(craftingRecipe.getResult());
                result.setAmount(craftingRecipe.getAmount());
                // lets check what we need as ingredients
                List<TCraftingRecipeIngredient> ingredients = craftingRecipe.getIngredients();
                if (ingredients.isEmpty()) {
                    plugin.getLogger().warning("The recipe " + craftingRecipe.getName() + " has no ingredients defined!");
                    continue;
                }

                switch (craftingRecipe.getType()) {

                    case FURNACE:
                        recipe = new FurnaceRecipe(result, RaidCraft.getItem(ingredients.get(0).getItem()).getData());
                        break;
                    case SHAPELESS:
                        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(result);
                        for (TCraftingRecipeIngredient ingredient : ingredients) {
                            shapelessRecipe.addIngredient(ingredient.getAmount(), RaidCraft.getItem(ingredient.getItem()).getData());
                        }
                        recipe = shapelessRecipe;
                        break;
                    case SHAPED:
                        ShapedRecipe shapedRecipe = new ShapedRecipe(result);
                        for (TCraftingRecipeIngredient ingredient : ingredients) {
                            shapedRecipe.setIngredient(ingredient.getSlot(), RaidCraft.getItem(ingredient.getItem()).getData());
                        }
                        shapedRecipe.setIngredient('O', Material.AIR);
                        shapedRecipe.setIngredient('o', Material.AIR);
                        shapedRecipe.shape(craftingRecipe.getShape().split("\\|"));
                        recipe = shapedRecipe;
                        break;
                }

                if (recipe != null) {
                    Bukkit.addRecipe(recipe);
                    loadedRecipes++;
                }
            } catch (CustomItemException e) {
                plugin.getLogger().warning(e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + loadedRecipes + "/" + recipes.size() + " custom crafting recipes");
    }
}
