package de.raidcraft.items.trigger;

import com.sk89q.worldedit.entity.Player;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.trigger.Trigger;
import de.raidcraft.api.items.ArmorType;
import de.raidcraft.api.items.CustomArmor;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.CustomWeapon;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.api.items.WeaponType;
import de.raidcraft.api.items.attachments.UseableCustomItem;
import de.raidcraft.items.crafting.CraftingManager;
import de.raidcraft.items.crafting.recipes.CustomRecipe;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.FurnaceInventory;

/**
 * @author mdoering
 */
public class CustomItemTrigger extends Trigger implements Listener {

    public CustomItemTrigger() {

        super("item", "pickup");
    }

    @Information(
            value = "item.pickup",
            desc = "Listens for players that pickup custom items.",
            help = "Take the item you want to listen for into your hand. " +
                    "You can then check for the complete id (-i), the armor type (-a), the weapon type (-w), normal type (-t) " +
                    "or if the item needs to be useable.",
            conf = {
                    "type: ItemType.java",
                    "weapon-type: WeaponType.java",
                    "armor-type: ArmorType.java",
                    "id: (int) ItemID",
                    "useable: true/false",
                    "quality: ItemQuality.java"
            },
            usage = "[-u/-i/-t/-w/-a]",
            flags = "uitwa",
            multiSection = true
    )
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
            } else if (config.isSet("quality")) {
                ItemQuality quality = ItemQuality.fromString(config.getString("quality"));
                return quality != null && item.getQuality() == quality;
            }
            return true;
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemCraft(CraftItemEvent event) {

        informListeners("craft", (Player) event.getWhoClicked(), config -> {

            if (event.getInventory() instanceof FurnaceInventory) {
                return false;
            }
            if (!CustomItemUtil.isCustomItem(event.getRecipe().getResult())) {
                return false;
            }
            CustomRecipe customRecipe = RaidCraft.getComponent(CraftingManager.class).getMatchingRecipe(event.getRecipe());
            return customRecipe != null && (!config.isSet("recipe")
                    || customRecipe.getName().equalsIgnoreCase(config.getString("recipe")));
        });
    }
}
