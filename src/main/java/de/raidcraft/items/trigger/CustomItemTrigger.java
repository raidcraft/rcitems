package de.raidcraft.items.trigger;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.trigger.Trigger;
import de.raidcraft.api.config.builder.ConfigBuilder;
import de.raidcraft.api.config.builder.ConfigBuilderException;
import de.raidcraft.api.items.ArmorType;
import de.raidcraft.api.items.CustomArmor;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.CustomWeapon;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.api.items.WeaponType;
import de.raidcraft.api.items.attachments.UseableCustomItem;
import net.citizensnpcs.api.command.CommandContext;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * @author mdoering
 */
public class CustomItemTrigger extends Trigger implements Listener {

    public CustomItemTrigger() {

        super("item", "pickup");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemPickup(PlayerPickupItemEvent event) {

        informListeners("pickup", event.getPlayer(), config -> {

            if (!RaidCraft.isCustomItem(event.getItem().getItemStack())) return false;
            CustomItemStack customItem = RaidCraft.getCustomItem(event.getItem().getItemStack());
            CustomItem item = customItem.getItem();
            if (config.isSet("type")) {
                ItemType type = ItemType.fromString(config.getString("type"));
                return type != null && type == item.getType();
            } else if (config.isSet("weapon-type")) {
                WeaponType weaponType = WeaponType.fromString(config.getString("weapon-type"));
                if (weaponType == null) {
                    RaidCraft.LOGGER.warning("Invalid weapon type " + config.getString("weapon-type") + " in " + config.getRoot().getName());
                    return false;
                }
                return item instanceof CustomWeapon && ((CustomWeapon) item).getWeaponType() == weaponType;
            } else if (config.isSet("armor-type")) {
                ArmorType armorType = ArmorType.fromString(config.getString("armor-type"));
                if (armorType == null) {
                    RaidCraft.LOGGER.warning("Invalid armor type " + config.getString("armor-type") + " in " + config.getRoot().getName());
                    return false;
                }
                return item instanceof CustomArmor && ((CustomArmor) item).getArmorType() == armorType;
            } else if (config.isSet("id")) {
                return item.getId() == config.getInt("id");
            } else if (config.isSet("useable")) {
                return config.getBoolean("useable") && item instanceof UseableCustomItem;
            }
            return true;
        });
    }

    @Information(
            value = "item.pickup",
            desc = "Listens for players that pickup custom items.",
            help = "Take the item you want to listen for into your hand. " +
                    "You can then check for the complete id (-i), the armor type (-a), the weapon type (-w), normal type (-t) " +
                    "or if the item needs to be useable.",
            usage = "[-u/-i/-t/-w/-a]",
            flags = "uitwa",
            multiSection = true
    )
    public void pickup(ConfigBuilder builder, CommandContext args, Player player) throws ConfigBuilderException {

        ConfigurationSection config = createConfigSection(getInformation("item.pickup"));
        if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
            CustomItemStack customItem = RaidCraft.getCustomItem(player.getItemInHand());
            if (customItem == null) throw new ConfigBuilderException("You need to hold a custom item in your hand or nothing!");
            if (args.hasFlag('u')) {
                config.set("useable", args.hasFlag('u'));
            } else if (args.hasFlag('i')) {
                config.set("id", customItem.getItem().getId());
            } else if (args.hasFlag('t')) {
                config.set("type", customItem.getItem().getType());
            } else if (args.hasFlag('w')) {
                if (customItem.getItem() instanceof CustomWeapon) {
                    config.set("weapon-type", ((CustomWeapon) customItem.getItem()).getWeaponType());
                }
            } else if (args.hasFlag('a')) {
                if (customItem.getItem() instanceof CustomArmor) {
                    config.set("armor-type", ((CustomArmor) customItem.getItem()).getArmorType());
                }
            }
        }
        builder.append(this, config, getPath(), "item.pickup");
    }
}
