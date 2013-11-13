package de.raidcraft.items.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import de.raidcraft.api.storage.ItemStorage;
import de.raidcraft.api.storage.StorageException;
import de.raidcraft.items.ItemsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Silthus
 */
public class StoreCommands {

    private final ItemsPlugin plugin;

    public StoreCommands(ItemsPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = {"store", "save"},
            desc = "Stores current item in hand in database and returns id of stored object"
    )
    @CommandPermissions("rcitems.store")
    public void store(CommandContext args, CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) {
            throw new CommandException("Not a player!");
        }
        Player player = (Player) sender;

        if(player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
            throw new CommandException("Du hast kein Item in der Hand!");
        }

        final String storageName = "custom";
        ItemStorage itemStorage = new ItemStorage(storageName);
        int itemId = itemStorage.storeObject(player.getItemInHand());
        sender.sendMessage(ChatColor.GREEN + "Item gespeichert -> Storage-Name: " + storageName + " | Storage-ID: " + itemId);
    }

    @Command(
            aliases = {"delete", "remove"},
            desc = "Delete stored item",
            min = 1
    )
    @CommandPermissions("rcitems.delete")
    public void delete(CommandContext args, CommandSender sender) throws CommandException {

        final String storageName = "custom";
        ItemStorage itemStorage = new ItemStorage(storageName);
        try {
            itemStorage.removeObject(args.getInteger(0));
        } catch (StorageException e) {
            throw new CommandException("Es wurde kein Item gefunden mit der Storage-ID " + args.getInteger(0));
        }
        sender.sendMessage(ChatColor.GREEN + "Das Storage-Item mit der ID: " + args.getInteger(0) + " wurde gelöscht!");
    }
}
