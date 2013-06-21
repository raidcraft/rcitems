package de.raidcraft.items.crafting.recipes;

import com.avaje.ebean.EbeanServer;
import de.raidcraft.RaidCraft;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.items.crafting.CraftingRecipeType;
import de.raidcraft.items.tables.TCraftingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

/**
 * @author Silthus
 */
public class CustomFurnaceRecipe extends FurnaceRecipe {

    private final String name;
    private String description;
    private ItemStack input;

    public CustomFurnaceRecipe(String name, ItemStack result, ItemStack input) {

        super(result, input.getData());
        this.name = name;
        this.input = input;
    }

    public String getName() {

        return name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
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

    public void save() {

        EbeanServer database = RaidCraft.getDatabase(ItemsPlugin.class);
        TCraftingRecipe recipe = database.find(TCraftingRecipe.class).where().eq("name", getName()).findUnique();
        if (recipe == null) {
            // create new
            recipe = new TCraftingRecipe();
        }
        recipe.setResult(RaidCraft.getItemIdString(getResult()));
        recipe.setAmount(getResult().getAmount());
        recipe.setDescription(getDescription());
        recipe.setShape(null);
        recipe.setType(CraftingRecipeType.FURNACE);
        database.save(recipe);
        // create the ingredients
        database.delete(recipe.getIngredients());
    }
}
