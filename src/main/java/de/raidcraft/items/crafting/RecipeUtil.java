package de.raidcraft.items.crafting;

/**
 * @author Silthus
 */

import de.raidcraft.RaidCraft;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class to compare Bukkit recipes.<br>
 * Useful for identifying your recipes in events, where recipes are re-generated in a diferent manner.
 *
 * @author Digi
 * @version R1.3
 */
public class RecipeUtil {

    /**
     * The wildcard data value for ingredients.<br>
     * If this is used as data value on an ingredient it will accept any data value.
     */
    public static final short DATA_WILDCARD = Short.MAX_VALUE;
    public static final char SHAPE_AIR_CHAR = ' ';

    /**
     * Checks if both recipes are equal.<br>
     * Compares both ingredients and results.<br>
     * <br>
     * NOTE: If both arguments are null it returns true.
     *
     * @param original
     * @param customRecipe
     *
     * @return true if ingredients and results match, false otherwise.
     *
     * @throws IllegalArgumentException if recipe is other than ShapedRecipe, ShapelessRecipe or FurnaceRecipe.
     */
    public static boolean areEqual(Recipe original, Recipe customRecipe) {

        if (original == customRecipe) {
            return true; // if they're the same instance (or both null) then they're equal.
        }

        if (original == null || customRecipe == null) {
            return false; // if only one of them is null then they're surely not equal.
        }

        if (!original.getResult().equals(customRecipe.getResult())) {
            return false; // if results don't match then they're not equal.
        }

        return match(original, customRecipe); // now check if ingredients match
    }

    /**
     * Checks if recipes are similar.<br>
     * Only checks ingredients, not results.<br>
     * <br>
     * NOTE: If both arguments are null it returns true. <br>
     *
     * @param recipe1
     * @param recipe2
     *
     * @return true if ingredients match, false otherwise.
     *
     * @throws IllegalArgumentException if recipe is other than ShapedRecipe, ShapelessRecipe or FurnaceRecipe.
     */
    public static boolean areSimilar(Recipe recipe1, Recipe recipe2) {

        if (recipe1 == recipe2) {
            return true; // if they're the same instance (or both null) then they're equal.
        }

        if (recipe1 == null || recipe2 == null) {
            return false; // if only one of them is null then they're surely not equal.
        }

        return match(recipe1, recipe2); // now check if ingredients match
    }

    private static boolean match(Recipe original, Recipe customRecipe) {

        if (original instanceof ShapedRecipe) {
            if (!(customRecipe instanceof ShapedRecipe)) {
                return false; // if other recipe is not the same type then they're not equal.
            }

            ShapedRecipe r1 = (ShapedRecipe) original;
            ShapedRecipe r2 = (ShapedRecipe) customRecipe;

            // convert both shapes and ingredient maps to common ItemStack array.
            ItemStack[] matrix1 = shapeToMatrix(r1.getShape(), r1.getIngredientMap());
            ItemStack[] matrix2 = shapeToMatrix(r2.getShape(), r2.getIngredientMap());

            if (!isEqualMatrix(matrix1, matrix2)) // compare arrays and if they don't match run another check with one shape mirrored.
            {
                mirrorMatrix(matrix1);

                return isEqualMatrix(matrix1, matrix2);
            }

            return true; // ingredients match.
        } else if (original instanceof ShapelessRecipe) {
            if (!(customRecipe instanceof ShapelessRecipe)) {
                return false; // if other recipe is not the same type then they're not equal.
            }

            ShapelessRecipe r1 = (ShapelessRecipe) original;
            ShapelessRecipe r2 = (ShapelessRecipe) customRecipe;

            // get copies of the ingredient lists
            List<ItemStack> find = r1.getIngredientList();
            List<ItemStack> compare = r2.getIngredientList().stream()
                    .map(i -> new ItemStack(i.getType(), i.getAmount(), i.getDurability()))
                    .collect(Collectors.toList());

            if (find.size() != compare.size()) {
                return false; // if they don't have the same amount of ingredients they're not equal.
            }

            for (ItemStack item : compare) {
                if (!find.remove(item)) {
                    return false; // if ingredient wasn't removed (not found) then they're not equal.
                }
            }

            return find.isEmpty(); // if there are any ingredients not removed then they're not equal.
        } else if (original instanceof FurnaceRecipe) {
            if (!(customRecipe instanceof FurnaceRecipe)) {
                return false; // if other recipe is not the same type then they're not equal.
            }

            FurnaceRecipe r1 = (FurnaceRecipe) original;
            FurnaceRecipe r2 = (FurnaceRecipe) customRecipe;

            //return (r1.getInput().equals(r2.getInput())); // TODO use this when furnace data PR is pulled
            return r1.getInput().getTypeId() == r2.getInput().getTypeId();
        } else {
            throw new IllegalArgumentException("Unsupported recipe type: '" + original + "', update this class!");
        }
    }

    public static boolean isEqualTypeMatrix(ItemStack[] matrix1, ItemStack[] matrix2) {

        if (matrix1.length != matrix2.length) {
            return false;
        }
        for (int i = 0; i < matrix1.length; i++) {
            boolean stack1Valid = ItemUtils.isStackValid(matrix1[i]);
            boolean stack2Valid = ItemUtils.isStackValid(matrix2[i]);
            if (!stack1Valid || !stack2Valid) {
                if (!stack1Valid && !stack2Valid) {
                    continue;
                }
                return false;
            }
            if (!matrix1[i].getData().equals(matrix2[i].getData())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEqualMatrix(ItemStack[] matrix1, ItemStack[] matrix2) {

        for (int i = 0; i < 9; i++) {
            boolean stack1Valid = ItemUtils.isStackValid(matrix1[i]);
            boolean stack2Valid = ItemUtils.isStackValid(matrix2[i]);
            if (!stack1Valid || !stack2Valid) {
                if (!stack1Valid && !stack2Valid) {
                    continue;
                }
                return false;
            }
            if (!RaidCraft.getItemIdString(matrix1[i]).equals(RaidCraft.getItemIdString(matrix2[i]))) {
                return false;
            }
        }
        return true;
    }

    public static ItemStack[] shapeToMatrix(String[] shape, Map<Character, ItemStack> map) {

        ItemStack[] matrix = new ItemStack[9];
        ItemStack air = new ItemStack(Material.AIR, 0);
        int slot = 0;

        for (int r = 0; r < shape.length; r++) {
            for (char col : shape[r].toCharArray()) {
                matrix[slot] = map.get(col);
                if (matrix[slot] == null) {
                    matrix[slot] = air;
                }
                slot++;
            }

            slot = ((r + 1) * 3);
        }

        return matrix;
    }

    public static void mirrorMatrix(ItemStack[] matrix) {

        ItemStack tmp;

        for (int r = 0; r < 3; r++) {
            tmp = matrix[(r * 3)];
            matrix[(r * 3)] = matrix[(r * 3) + 2];
            matrix[(r * 3) + 2] = tmp;
        }
    }
}