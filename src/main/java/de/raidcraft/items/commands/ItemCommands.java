package de.raidcraft.items.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemManager;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.items.ItemsPlugin;
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
            aliases = "rci",
            desc = "Gives a custom item to the player",
            min = 1
    )
    @CommandPermissions("rcitems.give")
    public void giveItem(CommandContext args, CommandSender sender) {

        CustomItemStack itemStack = RaidCraft.getComponent(CustomItemManager.class).getCustomItemStack(args.getInteger(0));
        ((Player) sender).getInventory().addItem(itemStack.getHandle());
    }
}
