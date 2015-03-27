package de.raidcraft.items.trigger;

import com.sk89q.worldedit.entity.Player;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.trigger.Trigger;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.ItemBindType;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.items.crafting.CraftingManager;
import de.raidcraft.items.crafting.recipes.CustomRecipe;
import de.raidcraft.util.ConfigUtil;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.FurnaceInventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

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

        if (!RaidCraft.isCustomItem(event.getItem().getItemStack())) return;
        CustomItem customItem = RaidCraft.getCustomItem(event.getItem().getItemStack()).getItem();

        informListeners("pickup", event.getPlayer(), config -> {

            int id = config.getInt("id");

            if (id > 0) return customItem.getId() == id;

            List<ItemType> itemTypes = new ArrayList<>();
            for (String type : config.getStringList("types")) {
                ItemType itemType = ItemType.fromString(type);
                if (itemType != null) {
                    itemTypes.add(itemType);
                } else {
                    RaidCraft.LOGGER.warning("Invalid item type " + type + " in loot table " + ConfigUtil.getFileName(config));
                }
            }

            List<ItemQuality> itemQualities = new ArrayList<>();
            for (String quality : config.getStringList("qualities")) {
                ItemQuality itemQuality = ItemQuality.fromString(quality);
                if (itemQuality != null) {
                    itemQualities.add(itemQuality);
                } else {
                    RaidCraft.LOGGER.warning("Invalid item quality " + quality + " in loot table " + ConfigUtil.getFileName(config));
                }
            }

            List<ItemBindType> bindTypes = new ArrayList<>();
            for (String binding : config.getStringList("bind-types")) {
                ItemBindType itemBindType = ItemBindType.fromString(binding);
                if (itemBindType != null) {
                    bindTypes.add(itemBindType);
                } else {
                    RaidCraft.LOGGER.warning("Invalid item bind type " + binding + " in loot table " + ConfigUtil.getFileName(config));
                }
            }

            List<Integer> itemIds = config.getIntegerList("ids");

            Pattern nameFilter;
            if (config.isSet("name-filter")) {
                nameFilter = Pattern.compile(config.getString("name-filter"));
            } else {
                nameFilter = null;
            }

            int idFilterMin = config.getInt("min-id", 0);
            int idFilterMax = config.getInt("max-id", 0);

            Collection<CustomItem> items = new ArrayList<>();
            items.add(customItem);
            return items.stream()
                    .filter(item -> itemTypes.isEmpty() || itemTypes.contains(item.getType()))
                    .filter(item -> itemQualities.isEmpty() || itemQualities.contains(item.getQuality()))
                    .filter(item -> bindTypes.isEmpty() || bindTypes.contains(item.getBindType()))
                    .filter(item -> itemIds.isEmpty() || itemIds.contains(item.getId()))
                    .filter(item -> idFilterMin < 1 || item.getId() >= idFilterMin)
                    .filter(item -> idFilterMax < 1 || item.getId() <= idFilterMax)
                    .filter(item -> nameFilter == null || nameFilter.matcher(item.getName()).matches()).count() > 0;
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
