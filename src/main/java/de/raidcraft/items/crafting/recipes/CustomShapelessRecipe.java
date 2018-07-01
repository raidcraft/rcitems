package de.raidcraft.items.crafting.recipes;

import com.avaje.ebean.EbeanServer;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.items.crafting.CraftingRecipeType;
import de.raidcraft.items.tables.crafting.TCraftingRecipe;
import de.raidcraft.items.tables.crafting.TCraftingRecipeIngredient;
import de.raidcraft.util.ItemUtils;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Silthus
 */
public class CustomShapelessRecipe extends ShapelessRecipe implements CustomRecipe {

    private final String name;
    private final String permission;
    private final Map<String, Integer> ingredients = new HashMap<>();

    public CustomShapelessRecipe(String name, String permission, ItemStack result) {

        super(result);
        this.name = name;
        this.permission = permission;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public String getPermission() {

        return permission;
    }

    @Override
    public CraftingRecipeType getType() {

        return CraftingRecipeType.SHAPELESS;
    }

    public CustomShapelessRecipe addIngredient(int amount, ItemStack ingredient) {

        if (amount < 1) amount = 1;
        super.addIngredient(amount, ingredient.getData());
        // lets query for the displayName of the custom item stack and add it to our list
        String item = RaidCraft.getItemIdString(ingredient);
        // dont allow item amounts greater than nine, because that makes no sense...
        if (amount > 9) amount = 9;
        while (amount-- > 0) {
            ingredients.put(item, amount);
        }
        return this;
    }

    public CustomShapelessRecipe removeIngredient(int amount, ItemStack ingredient) {

        super.removeIngredient(amount, ingredient.getData());
        String item = RaidCraft.getItemIdString(ingredient);
        while (amount-- > 0) {
            ingredients.remove(item);
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
                items.add(item);
            } catch (CustomItemException e) {
                RaidCraft.LOGGER.warning(e.getMessage());
            }
        }
        return items;
    }

    @Override
    public boolean isMatchingRecipe(CraftingInventory inventory) {

        ArrayList<String> remainingIngredients = new ArrayList<>(ingredients.keySet());

        for (ItemStack itemStack : inventory.getMatrix()) {
            if (!ItemUtils.isStackValid(itemStack)) {
                continue;
            }
            String id = RaidCraft.getItemIdString(itemStack);
            if (!remainingIngredients.remove(id)) {
                return false;
            }
            if (itemStack.getAmount() < ingredients.get(id)) {
                return false;
            }
        }
        return remainingIngredients.isEmpty();
    }

    @Override
    public void save() {

        EbeanServer database = RaidCraft.getDatabase(ItemsPlugin.class);
        TCraftingRecipe recipe = database.find(TCraftingRecipe.class).where().eq("name", getName()).findUnique();
        if (recipe == null) {
            // create new
            recipe = new TCraftingRecipe();
        }
        recipe.setName(getName());
        recipe.setResult(RaidCraft.getItemIdString(getResult()));
        recipe.setAmount(getResult().getAmount());
        recipe.setShape(null);
        recipe.setType(CraftingRecipeType.SHAPELESS);
        recipe.setPermission(getPermission());
        database.save(recipe);
        // create the ingredients
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            database.delete(recipe.getIngredients());
        }
        for (ItemStack itemStack : getIngredientList()) {
            TCraftingRecipeIngredient ingredient = new TCraftingRecipeIngredient();
            ingredient.setItem(RaidCraft.getItemIdString(itemStack));
            ingredient.setAmount(1);
            ingredient.setRecipe(recipe);
            database.save(ingredient);
        }
    }

    @Override
    public String toString() {

        return getName();
    }
}
