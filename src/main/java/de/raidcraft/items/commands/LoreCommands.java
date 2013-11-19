package de.raidcraft.items.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.util.SignUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * @author Silthus
 */
public class LoreCommands {

    private final ItemsPlugin plugin;

    public LoreCommands(ItemsPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = {"addline", "add"},
            desc = "Add lore line",
            min = 1,
            usage = "<lore line>"
    )
    @CommandPermissions("rcitems.lore")
    public void addLine(CommandContext args, CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) {
            throw new CommandException("Not a player!");
        }
        Player player = (Player) sender;

        if(player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
            throw new CommandException("Du hast kein Item in der Hand!");
        }

        ItemStack itemStack = player.getItemInHand();
        String newLoreLine = SignUtil.parseColor(args.getJoinedStrings(0));

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getLore().add(newLoreLine);
        itemStack.setItemMeta(itemMeta);

        player.updateInventory();
        player.sendMessage(ChatColor.GREEN + "Die Item-Lore wurde geändert!");
    }

    @Command(
            aliases = {"remove", "delete"},
            desc = "Remove lore"
    )
    @CommandPermissions("rcitems.lore")
    public void remove(CommandContext args, CommandSender sender) throws CommandException {

        if (!(sender instanceof Player)) {
            throw new CommandException("Not a player!");
        }
        Player player = (Player) sender;

        if(player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
            throw new CommandException("Du hast kein Item in der Hand!");
        }

        ItemStack itemStack = player.getItemInHand();

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(new ArrayList<String>());
        itemStack.setItemMeta(itemMeta);

        player.updateInventory();
        player.sendMessage(ChatColor.GREEN + "Die Item-Lore wurde entfernt!");
    }
}
