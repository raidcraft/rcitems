package de.raidcraft.items.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.items.crafting.CraftingRecipeType;
import de.raidcraft.items.crafting.recipes.CustomFurnaceRecipe;
import de.raidcraft.items.crafting.recipes.CustomShapedRecipe;
import de.raidcraft.items.crafting.recipes.CustomShapelessRecipe;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

        ItemStack result = player.getInventory().getItem(RESULT_SLOT);
        if (!ItemUtils.isStackValid(result)) {
            throw new CommandException("Es wird ein gültiges Ergebnis benötigt! " +
                    "Bitte lege das Ergebnis Item in die Mitte deines Inventars in der 4. Spalte von links.");
        }

        // we will use the inventory slots on the left (3x3)
        // and the one in the middle will be the result
        ItemStack[] slots = new ItemStack[]{((Player) sender).getInventory().getItem(9),((Player) sender).getInventory().getItem(10),
                ((Player) sender).getInventory().getItem(11),((Player) sender).getInventory().getItem(18),((Player) sender).getInventory().getItem(19),
                ((Player) sender).getInventory().getItem(20),((Player) sender).getInventory().getItem(27),((Player) sender).getInventory().getItem(28),
                ((Player) sender).getInventory().getItem(29)};

        if(type == CraftingRecipeType.SHAPED) {

            LinkedHashMap<ItemStack, Character> items = new LinkedHashMap<>();

            int furtherestX = -1;
            int furtherestY = -1;

            for (int slot = 0; slot < 3; slot++) {
                ItemStack stack = slots[slot];
                if(ItemUtils.isStackValid(stack)) {
                    furtherestY = 0;
                    if(furtherestX < slot)
                        furtherestX = slot;
                }
            }
            for (int slot = 3; slot < 6; slot++) {
                ItemStack stack = slots[slot];
                if(ItemUtils.isStackValid(stack)) {
                    furtherestY = 1;
                    if(furtherestX < slot-3)
                        furtherestX = slot-3;
                }
            }
            for (int slot = 6; slot < 9; slot++) {
                ItemStack stack = slots[slot];
                if(ItemUtils.isStackValid(stack)) {
                    furtherestY = 2;
                    if(furtherestX < slot-6)
                        furtherestX = slot-6;
                }
            }

            if(furtherestX > 2)
                furtherestX = 2;

            String[] shape = new String[furtherestY+1];
            Character[] characters = new Character[]{'a','b','c','d','e','f','g','h','i'};
            int curChar = 0;

            for(int y = 0; y < furtherestY+1; y++) {
                for(int x = 0; x < furtherestX+1; x++) {

                    String c = " ";
                    ItemStack stack = slots[x+y*3];
                    if (ItemUtils.isStackValid(stack)) {

                        boolean found = false;
                        for (ItemStack st : items.keySet()) {
                            if (st.isSimilar(stack)) {
                                c = items.get(st).toString();
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            items.put(stack, characters[curChar]);
                            c = characters[curChar].toString();
                            curChar++;
                        }
                    }

                    if (x == 0) {
                        shape[y] = c;
                    } else {
                        shape[y] = shape[y] + c;
                    }
                }
            }

            CustomShapedRecipe recipe = new CustomShapedRecipe(name, permission, result);
            recipe.shape(shape);
            for (Map.Entry<ItemStack, Character> entry : items.entrySet()) {
                recipe.setIngredient(entry.getValue(), entry.getKey());
            }
            plugin.getCraftingManager().loadRecipe(recipe);
            recipe.save();

        } else if (type == CraftingRecipeType.SHAPELESS) {

            Map<ItemStack, Integer> ingredients = new HashMap<>();

            for (ItemStack slot : slots) {

                if (!ItemUtils.isStackValid(slot))
                    continue;

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

            if (slots.length < 1 || slots.length > 1) {
                throw new CommandException("Ungültige Zutatenmenge für ein Schmelz Rezept.");
            }
            CustomFurnaceRecipe recipe = new CustomFurnaceRecipe(name, permission, result, slots[0]);
            plugin.getCraftingManager().loadRecipe(recipe);
            recipe.save();
        }
        sender.sendMessage(ChatColor.GREEN + "Custom Crafting Rezept wurde erfolgreich hinzugefügt.");
    }
}
