package de.raidcraft.items.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemManager;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.items.ItemsPlugin;
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
            aliases = {"rci", "item", "rcitems"},
            desc = "Custom Item Commands",
            min = 1
    )

    @NestedCommand(value = SubCommands.class)
    public void giveItem(CommandContext args, CommandSender sender) {

    }

    public static class SubCommands {

        private final ItemsPlugin plugin;

        public SubCommands(ItemsPlugin plugin) {

            this.plugin = plugin;
        }

        @Command(
                aliases = "reload",
                desc = "Reloads the items plugin and all configs"
        )
        @CommandPermissions("rcitems.reload")
        public void reload(CommandContext args, CommandSender sender) {

            plugin.reload();
        }

        @Command(
                aliases = {"give", "i", "g"},
                desc = "Gives a custom item to the player",
                min = 1
        )
        @CommandPermissions("rcitems.give")
        public void give(CommandContext args, CommandSender sender) {

            try {
                CustomItemStack itemStack = RaidCraft.getComponent(CustomItemManager.class).getCustomItemStack(args.getInteger(0));
                ((Player) sender).getInventory().addItem(itemStack.getHandle());
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
}
