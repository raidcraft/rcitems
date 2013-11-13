package de.raidcraft.items.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import de.raidcraft.items.ItemsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

/**
 * @author Silthus
 */
public class BookUtilCommands {

    private final ItemsPlugin plugin;

    public BookUtilCommands(ItemsPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = {"author", "autor", "setauthor"},
            desc = "Modifies book author",
            min = 1,
            usage = "<new author>"
    )
    @CommandPermissions("rcitems.book.author")
    public void author(CommandContext args, CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) {
            throw new CommandException("Not a player!");
        }
        Player player = (Player) sender;

        if(player.getItemInHand() == null || player.getItemInHand().getType() != Material.WRITTEN_BOOK) {
            throw new CommandException("Du hast kein beschriebenes Buch in der Hand!");
        }

        String newAuthor = args.getJoinedStrings(0);

        BookMeta bookMeta = (BookMeta)player.getItemInHand().getItemMeta();
        bookMeta.setAuthor(newAuthor);
        player.getItemInHand().setItemMeta(bookMeta);
        player.updateInventory();

        sender.sendMessage(ChatColor.GREEN + "Der Autor des Buches wurde auf '" + newAuthor + "' geändert!");
    }
}
