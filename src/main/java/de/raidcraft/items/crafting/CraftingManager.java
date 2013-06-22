package de.raidcraft.items.crafting;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.Component;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.items.crafting.recipes.CustomFurnaceRecipe;
import de.raidcraft.items.crafting.recipes.CustomRecipe;
import de.raidcraft.items.crafting.recipes.CustomShapedRecipe;
import de.raidcraft.items.crafting.recipes.CustomShapelessRecipe;
import de.raidcraft.items.tables.crafting.TCraftingRecipe;
import de.raidcraft.items.tables.crafting.TCraftingRecipeIngredient;
import de.raidcraft.util.CaseInsensitiveMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.List;
import java.util.Map;

/**
 * @author Silthus
 */
public class CraftingManager implements Component {

    private final ItemsPlugin plugin;
    private final Map<String, CustomRecipe> loadedRecipes = new CaseInsensitiveMap<>();

    public CraftingManager(ItemsPlugin plugin) {

        this.plugin = plugin;
        loadRecipes();
        plugin.registerEvents(new CraftingListener(this));
    }

    public void reload() {

        Bukkit.clearRecipes();
        loadRecipes();
    }

    private void loadRecipes() {

        int loadedRecipes = 0;
        CustomRecipe recipe = null;
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
                        recipe = new CustomFurnaceRecipe(
                                craftingRecipe.getName(),
                                craftingRecipe.getPermission(),
                                result,
                                RaidCraft.getItem(ingredients.get(0).getItem())
                        );
                        break;
                    case SHAPELESS:
                        CustomShapelessRecipe shapelessRecipe = new CustomShapelessRecipe(
                                craftingRecipe.getName(),
                                craftingRecipe.getPermission(),
                                result
                        );
                        for (TCraftingRecipeIngredient ingredient : ingredients) {
                            shapelessRecipe.addIngredient(ingredient.getAmount(), RaidCraft.getItem(ingredient.getItem()).getData());
                        }
                        recipe = shapelessRecipe;
                        break;
                    case SHAPED:
                        CustomShapedRecipe shapedRecipe = new CustomShapedRecipe(
                                craftingRecipe.getName(),
                                craftingRecipe.getPermission(),
                                result
                        );
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
                    if (!loadRecipe(recipe)) {
                        plugin.getLogger().warning("Failed to load duplicate recipe: " + recipe.getName());
                    } else {
                        loadedRecipes++;
                    }
                }
            } catch (CustomItemException e) {
                plugin.getLogger().warning(e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + loadedRecipes + "/" + recipes.size() + " custom crafting recipes");
    }

    public boolean loadRecipe(CustomRecipe recipe) {

        if (loadedRecipes.containsKey(recipe.getName())) {
            return false;
        }
        Bukkit.addRecipe(recipe);
        this.loadedRecipes.put(recipe.getName(), recipe);
        return true;
    }

    public boolean isLoadedRecipe(String name) {

        return loadedRecipes.containsKey(name);
    }

    public CustomRecipe getRecipe(String name) {

        return loadedRecipes.get(name);
    }

    public CustomRecipe getMatchingRecipe(Recipe recipe) {

        for (CustomRecipe customRecipe : loadedRecipes.values()) {
            if (customRecipe.equals(recipe)) {
                return customRecipe;
            }
        }
        return null;
    }
}
