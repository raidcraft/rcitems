package de.raidcraft.items.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.items.ItemsPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author Silthus
 */
public class RecipeCommands {

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
    public void create(CommandContext args, CommandSender sender) {

        Player player = (Player) sender;
        PlayerInventory inventory = player.getInventory();
        // we will use the inventory slots on the left (3x3)
        // and the one in the middle will be the result
    }
}
