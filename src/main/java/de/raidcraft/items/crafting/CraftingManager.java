package de.raidcraft.items.crafting;

import com.sk89q.util.StringUtil;
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
import org.bukkit.inventory.*;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
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
        reload();
        plugin.registerEvents(new CraftingListener(this));
    }

    public void reload() {

        loadedRecipes.clear();
        furnaceRecipes.clear();
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
                result = RaidCraft.getSafeItem(craftingRecipe.getResult());
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
                                new ItemStack(RaidCraft.getSafeItem(ingredients.get(0).getItem()))
                        );
                        break;
                    case SHAPELESS:
                        CustomShapelessRecipe shapelessRecipe = new CustomShapelessRecipe(
                                craftingRecipe.getName(),
                                craftingRecipe.getPermission(),
                                result
                        );
                        for (TCraftingRecipeIngredient ingredient : ingredients) {
                            shapelessRecipe.addIngredient(ingredient.getAmount(), new ItemStack(RaidCraft.getSafeItem(ingredient.getItem())));
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
                            shapedRecipe.setIngredient(ingredient.getSlot(), new ItemStack(RaidCraft.getSafeItem(ingredient.getItem())));
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

    public CustomRecipe getRecipe(String name) throws UnknownRecipeException {

        if (isLoadedRecipe(name)) {
            return loadedRecipes.get(name);
        }
        List<CustomRecipe> foundRecipes = new ArrayList<>();
        for (CustomRecipe recipe : loadedRecipes.values()) {
            if (recipe.getName().contains(name)) {
                foundRecipes.add(recipe);
            }
        }
        if (foundRecipes.size() == 1) {
            return foundRecipes.get(0);
        }
        if (foundRecipes.isEmpty()) {
            throw new UnknownRecipeException("There is no recipe with the displayName " + name);
        }
        throw new UnknownRecipeException("Found multiple recipes with the displayName " + name + ": "
                + StringUtil.joinString(foundRecipes, ", ", 0));
    }

    public CustomRecipe deleteRecipe(String name) throws UnknownRecipeException {

        CustomRecipe recipe = getRecipe(name);
        TCraftingRecipe table = plugin.getDatabase().find(TCraftingRecipe.class).where().eq("name", recipe.getName()).findOne();
        plugin.getDatabase().delete(table);
        loadedRecipes.remove(recipe.getName());
        return recipe;
    }

    public CustomFurnaceRecipe getFurnaceRecipe(ItemStack input) {

        return furnaceRecipes.get(input.getData());
    }

    public CustomRecipe getMatchingRecipe(CraftingInventory input) {

        for (CustomRecipe customRecipe : loadedRecipes.values()) {
            if (customRecipe.isMatchingRecipe(input)) {
                return customRecipe;
            }
        }
        return null;
    }

    public CustomRecipe getMatchingRecipe(Recipe recipe) {

        for (CustomRecipe customRecipe : loadedRecipes.values()) {
            if (RecipeUtil.areEqual(recipe, customRecipe)) {
                return customRecipe;
            }
        }
        return null;
    }
}
