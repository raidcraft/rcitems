package de.raidcraft.items.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemManager;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.items.ItemsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Silthus
 */
public class ItemCommands {

    private final ItemsPlugin plugin;

    public ItemCommands(ItemsPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = "reload",
            desc = "Reloads the items plugin and all configs"
    )
    @CommandPermissions("rcitems.reload")
    public void reload(CommandContext args, CommandSender sender) {

        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "Es wurden alle Custom Items und Rezepte erfolgreich neugeladen.");
    }

    @Command(
            aliases = {"give", "i", "g"},
            desc = "Gives a custom item to the player",
            min = 1,
            flags = "p:"
    )
    @CommandPermissions("rcitems.give")
    public void give(CommandContext args, CommandSender sender) throws CommandException {

        try {
            Player player;
            if (args.hasFlag('p')) {
                player = Bukkit.getPlayer(args.getFlag('p'));
                if (player == null) {
                    throw new CommandException("Es ist kein Spieler mit dem Namen " + args.hasFlag('p') + " online.");
                }
            } else {
                player = (Player) sender;
            }
            CustomItemStack itemStack = RaidCraft.getComponent(CustomItemManager.class).getCustomItemStack(args.getJoinedStrings(0));
            player.getInventory().addItem(itemStack.getHandle());
            player.sendMessage(ChatColor.GREEN + "Dir wurde das Custom Item " + ChatColor.AQUA + itemStack.getItem().getName() + ChatColor.GREEN + " gegeben.");
            if (!player.equals(sender)) {
                sender.sendMessage(ChatColor.GREEN + "Du hast " + ChatColor.AQUA + player.getName() + ChatColor.GREEN
                        + " das Custom Item " + ChatColor.AQUA + itemStack.getItem().getName() + ChatColor.GREEN + " gegeben.");
            }
        } catch (CustomItemException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            plugin.getLogger().warning(e.getMessage());
        }
    }

    @Command(
            aliases = {"wizard", "config", "new", "create"},
            desc = "Creates a new Item in the Item Wizard."
    )
    @CommandPermissions("rcitems.create")
    public void wizard(CommandContext args, CommandSender sender) {


    }
}
