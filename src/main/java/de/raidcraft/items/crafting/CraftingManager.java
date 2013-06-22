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
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Silthus
 */
public class CraftingManager implements Component {

    private final ItemsPlugin plugin;
    private final Map<String, CustomRecipe> loadedRecipes = new CaseInsensitiveMap<>();
    private final Map<MaterialData, CustomFurnaceRecipe> furnaceRecipes = new HashMap<>();

    public CraftingManager(ItemsPlugin plugin) {

        this.plugin = plugin;
        loadRecipes();
        plugin.registerEvents(new CraftingListener(this));
    }

    public void reload() {

        Bukkit.resetRecipes();
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
                                new ItemStack(RaidCraft.getItem(ingredients.get(0).getItem()))
                        );
                        break;
                    case SHAPELESS:
                        CustomShapelessRecipe shapelessRecipe = new CustomShapelessRecipe(
                                craftingRecipe.getName(),
                                craftingRecipe.getPermission(),
                                result
                        );
                        for (TCraftingRecipeIngredient ingredient : ingredients) {
                            shapelessRecipe.addIngredient(ingredient.getAmount(), new ItemStack(RaidCraft.getItem(ingredient.getItem())));
                        }
                        recipe = shapelessRecipe;
                        break;
                    case SHAPED:
                        CustomShapedRecipe shapedRecipe = new CustomShapedRecipe(
                                craftingRecipe.getName(),
                                craftingRecipe.getPermission(),
                                result
                        );
                        shapedRecipe.shape(craftingRecipe.getShape().split("\\|"));
                        for (TCraftingRecipeIngredient ingredient : ingredients) {
                            shapedRecipe.setIngredient(ingredient.getSlot(), new ItemStack(RaidCraft.getItem(ingredient.getItem())));
                        }
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

    public CraftingRecipeType getType(Recipe recipe) {

        if (recipe instanceof FurnaceRecipe) return CraftingRecipeType.FURNACE;
        if (recipe instanceof ShapedRecipe) return CraftingRecipeType.SHAPED;
        return CraftingRecipeType.SHAPELESS;
    }

    public boolean loadRecipe(CustomRecipe recipe) {

        if (loadedRecipes.containsKey(recipe.getName())) {
            return false;
        }
        if (recipe instanceof CustomFurnaceRecipe) {
            if (furnaceRecipes.containsKey(((CustomFurnaceRecipe) recipe).getInput().getData())) {
                return false;
            }
            furnaceRecipes.put(((CustomFurnaceRecipe) recipe).getInput().getData(), (CustomFurnaceRecipe) recipe);
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

    public CustomFurnaceRecipe getFurnaceRecipe(ItemStack input) {

        return furnaceRecipes.get(input.getData());
    }

    @SuppressWarnings("ConstantConditions")
    public CustomRecipe getMatchingRecipe(Recipe recipe) {

        for (CustomRecipe customRecipe : loadedRecipes.values()) {
            if (RecipeUtil.areEqual(recipe, customRecipe)) {
                return customRecipe;
            }
        }
        return null;
    }
}
