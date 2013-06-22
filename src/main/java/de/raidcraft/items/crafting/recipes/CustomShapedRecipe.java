package de.raidcraft.items.crafting.recipes;

import com.avaje.ebean.EbeanServer;
import de.raidcraft.RaidCraft;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.items.crafting.CraftingRecipeType;
import de.raidcraft.items.tables.crafting.TCraftingRecipe;
import de.raidcraft.items.tables.crafting.TCraftingRecipeIngredient;
import de.raidcraft.util.CustomItemUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Silthus
 */
public class CustomShapedRecipe extends ShapedRecipe implements CustomRecipe {

    private final String name;
    private final String permission;
    private final Map<Character, ItemStack> ingredients = new HashMap<>();

    public CustomShapedRecipe(String name, String permission, ItemStack result) {

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

        return CraftingRecipeType.SHAPED;
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

    @Override
    public boolean isMatchingRecipe(CraftingInventory inventory) {

        ItemStack[] input = inventory.getMatrix();
        ItemStack[] required = buildRequiredInput(input.length);

        for (int i = 0; i < input.length; i++) {
            if (!CustomItemUtil.isEqualCustomItem(input[i], required[i])) {
                return false;
            }
        }
        return true;
    }

    private ItemStack[] buildRequiredInput(int length) {

        ItemStack[] out = new ItemStack[length];
        String[] shape = getShape();

        for (int i = 0; i < length; i++) {
            out[i] = ingredients.get(shape[i].charAt(0));
        }
        return out;
    }

    @Override
    public void save() {

        EbeanServer database = RaidCraft.getDatabase(ItemsPlugin.class);
        TCraftingRecipe recipe = database.find(TCraftingRecipe.class).where().eq("name", getName()).findUnique();
        if (recipe == null) {
            // create new
            recipe = new TCraftingRecipe();
        }
        recipe.setResult(RaidCraft.getItemIdString(getResult()));
        recipe.setAmount(getResult().getAmount());
        recipe.setShape(StringUtils.join(getShape(), "|"));
        recipe.setType(CraftingRecipeType.SHAPED);
        recipe.setPermission(getPermission());
        database.save(recipe);
        // create the ingredients
        database.delete(recipe.getIngredients());
        for (Map.Entry<Character, ItemStack> entry : getIngredientMap().entrySet()) {
            TCraftingRecipeIngredient ingredient = new TCraftingRecipeIngredient();
            ingredient.setRecipe(recipe);
            ingredient.setAmount(entry.getValue().getAmount());
            ingredient.setSlot(entry.getKey());
            ingredient.setItem(RaidCraft.getItemIdString(entry.getValue()));
            database.save(ingredient);
        }
    }
}
