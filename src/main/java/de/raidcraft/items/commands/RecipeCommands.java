package de.raidcraft.items.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.items.crafting.CraftingRecipeType;
import de.raidcraft.items.crafting.RecipeUtil;
import de.raidcraft.items.crafting.UnknownRecipeException;
import de.raidcraft.items.crafting.recipes.CustomFurnaceRecipe;
import de.raidcraft.items.crafting.recipes.CustomRecipe;
import de.raidcraft.items.crafting.recipes.CustomShapedRecipe;
import de.raidcraft.items.crafting.recipes.CustomShapelessRecipe;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Silthus
 */
public class RecipeCommands {

    // its the slot in the middle 3 from the left
    // http://media-mcw.cursecdn.com/8/8c/Items_slot_number.JPG
    private static final int RESULT_SLOT = 21;

    private final ItemsPlugin plugin;

    public RecipeCommands(ItemsPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = {"create"},
            desc = "Creates a new recipe",
            min = 2,
            flags = "p:",
            usage = "<name> <type[SHAPED,SHAPELESS,FURNACE]>"
    )
    @CommandPermissions("rcitems.recipe.create")
    public void create(CommandContext args, CommandSender sender) throws CommandException {

        Player player = (Player) sender;
        String name = args.getString(0);
        CraftingRecipeType type = CraftingRecipeType.fromString(args.getString(1));
        String permission = null;

        if (args.hasFlag('p')) {
            permission = args.getFlag('p');
        }

        if (type == null) {
            throw new CommandException("Ungültiger Rezept Typ: " + args.getString(1));
        }

        if (plugin.getCraftingManager().isLoadedRecipe(name)) {
            throw new CommandException("Das Crafting Rezept " + name + " ist bereits registriert.");
        }

        ItemStack result = new ItemStack(player.getInventory().getItem(RESULT_SLOT));
        if (!ItemUtils.isStackValid(result)) {
            throw new CommandException("Es wird ein gültiges Ergebnis benötigt! " +
                    "Bitte lege das Ergebnis Item in die Mitte deines Inventars in der 4. Spalte von links.");
        }

        // we will use the inventory slots on the left (3x3)
        // and the one in the middle will be the result
        PlayerInventory inventory = ((Player) sender).getInventory();
        ItemStack[] slots = new ItemStack[]{inventory.getItem(9), inventory.getItem(10),
                inventory.getItem(11), inventory.getItem(18), inventory.getItem(19),
                inventory.getItem(20), inventory.getItem(27), inventory.getItem(28),
                inventory.getItem(29)};

        if(type == CraftingRecipeType.SHAPED) {

            LinkedHashMap<ItemStack, Character> items = new LinkedHashMap<>();

            String[] shape = new String[3];
            Character[] characters = new Character[]{'a','b','c','d','e','f','g','h','i'};
            int curChar = 0;

            int x = 0;
            String shapeLine = "";
            for(int i = 0; i < slots.length; i++) {

                char c = RecipeUtil.SHAPE_AIR_CHAR;
                ItemStack stack = slots[i];
                if (ItemUtils.isStackValid(stack)) {

                    boolean found = false;
                    for (ItemStack st : items.keySet()) {
                        if (st.isSimilar(stack)) {
                            c = items.get(st);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        items.put(stack, characters[curChar]);
                        c = characters[curChar];
                        curChar++;
                    }
                }
                shapeLine += c;
                if (shapeLine.length() % 3 == 0) {
                    shape[x] = shapeLine;
                    shapeLine = "";
                    x++;
                }
            }

            CustomShapedRecipe recipe = new CustomShapedRecipe(name, permission, result);
            recipe.shape(shape);
            for (Map.Entry<ItemStack, Character> entry : items.entrySet()) {
                recipe.setIngredient(entry.getValue(), new ItemStack(entry.getKey()));
            }
            plugin.getCraftingManager().loadRecipe(recipe);
            recipe.save();

        } else if (type == CraftingRecipeType.SHAPELESS) {

            Map<ItemStack, Integer> ingredients = new HashMap<>();

            for (ItemStack slot : slots) {

                if (!ItemUtils.isStackValid(slot)) {
                    continue;
                }

                ItemStack stack = new ItemStack(slot.clone());

                boolean used = false;
                for (ItemStack compare : ingredients.keySet()) {

                    if (CustomItemUtil.isEqualCustomItem(compare, stack)) {
                        ingredients.put(compare, ingredients.get(compare) + 1);
                        used = true;
                        break;
                    }
                }

                if (!used) {
                    ingredients.put(stack, 1);
                }
            }

            CustomShapelessRecipe recipe = new CustomShapelessRecipe(name, permission, result);
            for (Map.Entry<ItemStack, Integer> entry : ingredients.entrySet()) {
                recipe.addIngredient(entry.getValue(), entry.getKey());
            }
            plugin.getCraftingManager().loadRecipe(recipe);
            recipe.save();
        } else if (type == CraftingRecipeType.FURNACE) {

            ItemStack input = null;
            for (ItemStack slot : slots) {
                if (!ItemUtils.isStackValid(slot)) {
                    continue;
                }
                if (input != null) {
                    throw new CommandException("Ungültige Zutatenmenge für ein Schmelzrezept. Maximal eine Zutat erlaubt.");
                }
                input = slot;
            }
            CustomFurnaceRecipe recipe = new CustomFurnaceRecipe(name, permission, result, input);
            plugin.getCraftingManager().loadRecipe(recipe);
            recipe.save();
        }
        sender.sendMessage(ChatColor.GREEN + "Custom Crafting Rezept wurde erfolgreich hinzugefügt.");
    }

    @Command(
            aliases = {"remove", "delete", "del"},
            desc = "Removes the given recipe",
            min = 1,
            usage = "<name>"
    )
    @CommandPermissions("rcitems.recipe.remove")
    public void remove(CommandContext args, CommandSender sender) throws CommandException {

        try {
            CustomRecipe recipe = plugin.getCraftingManager().deleteRecipe(args.getString(0));
            sender.sendMessage(ChatColor.GREEN + "Deleted the Custom Crafting Recipe " + recipe.getName() + " sucessfully!");
        } catch (UnknownRecipeException e) {
            throw new CommandException(e.getMessage());
        }
    }
}
