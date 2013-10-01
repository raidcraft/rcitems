package de.raidcraft.items.crafting.recipes;

import com.avaje.ebean.EbeanServer;
import de.raidcraft.RaidCraft;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.items.crafting.CraftingRecipeType;
import de.raidcraft.items.tables.crafting.TCraftingRecipe;
import de.raidcraft.items.tables.crafting.TCraftingRecipeIngredient;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

/**
 * @author Silthus
 */
public class CustomFurnaceRecipe extends FurnaceRecipe implements CustomRecipe {

    private final String name;
    private final String permission;
    private ItemStack input;

    public CustomFurnaceRecipe(String name, String permission, ItemStack result, ItemStack input) {

        super(result, input.getData());
        this.name = name;
        this.permission = permission;
        this.input = input;
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

        return CraftingRecipeType.FURNACE;
    }

    @Override
    public ItemStack getInput() {

        return input;
    }

    public CustomFurnaceRecipe setInput(ItemStack itemStack) {

        super.setInput(itemStack.getData());
        this.input = itemStack;
        return this;
    }

    @Override
    public boolean isMatchingRecipe(CraftingInventory inventory) {

        return inventory instanceof FurnaceInventory && isMatchingRecipe(((FurnaceInventory) inventory).getSmelting());
    }

    public boolean isMatchingRecipe(ItemStack input) {

        return CustomItemUtil.isEqualCustomItem(input, getInput());
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
        recipe.setType(CraftingRecipeType.FURNACE);
        recipe.setPermission(getPermission());
        database.save(recipe);
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            // create the ingredients
            database.delete(recipe.getIngredients());
        }
        TCraftingRecipeIngredient ingredient = new TCraftingRecipeIngredient();
        ingredient.setAmount(getInput().getAmount());
        ingredient.setRecipe(recipe);
        ingredient.setItem(RaidCraft.getItemIdString(getInput()));
        database.save(ingredient);
    }

    @Override
    public String toString() {

        return getName();
    }
}
