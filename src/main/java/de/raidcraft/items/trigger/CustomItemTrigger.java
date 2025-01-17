package de.raidcraft.items.trigger;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.trigger.Trigger;
import de.raidcraft.api.items.*;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.items.crafting.recipes.CustomRecipe;
import de.raidcraft.util.ConfigUtil;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author mdoering
 */
public class CustomItemTrigger extends Trigger implements Listener {

    public CustomItemTrigger() {

        super("item", "pickup", "craft");
    }

    @Information(
            value = "item.pickup",
            desc = "Listens for players that pickup custom items.",
            conf = {
                    "id(int): DatabaseID (if set will only listen for this item)",
                    "types(StringList): <a href=\"https://git.raid-craft.de/raid-craft-de/raidcraft-api/blob/master/src/main/java/de/raidcraft/api/items/ItemType.java\">ItemType</a>",
                    "qualities(StringList): <a href=\"https://git.raid-craft.de/raid-craft-de/raidcraft-api/blob/master/src/main/java/de/raidcraft/api/items/ItemQuality.java\">ItemQuality</a>",
                    "bind-types(StringList): <a href=\"https://git.raid-craft.de/raid-craft-de/raidcraft-api/blob/master/src/main/java/de/raidcraft/api/items/ItemBindType.java\">BindType</a>",
                    "ids(IntList): List of item ids (database ids)",
                    "displayName-filter(RegExpPattern): Regex of Item Name",
                    "min-id(int): item db id for range",
                    "max-id(int): item db id for range"
            }
    )
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemPickup(PlayerPickupItemEvent event) {

        CustomItem customItem;
        if (RaidCraft.isCustomItem(event.getItem().getItemStack())) {
            customItem = RaidCraft.getCustomItem(event.getItem().getItemStack()).getItem();
            if (customItem.getType() != ItemType.QUEST && event.isCancelled()) return;
        } else {
            customItem = null;
        }

        informListeners("pickup", event.getPlayer(), config -> {

            if (config.isSet("item")) {
                return RaidCraft.getItem(config.getString("item"))
                        .map(itemStack -> itemStack.isSimilar(event.getItem().getItemStack()))
                        .orElse(false);
            }

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
            if (config.isSet("displayName-filter")) {
                nameFilter = Pattern.compile(config.getString("displayName-filter"));
            } else {
                nameFilter = null;
            }

            int idFilterMin = config.getInt("min-id", 0);
            int idFilterMax = config.getInt("max-id", 0);

            Collection<CustomItem> items = new ArrayList<>();
            items.add(customItem);
            return items.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> itemTypes.isEmpty() || itemTypes.contains(item.getType()))
                    .filter(item -> itemQualities.isEmpty() || itemQualities.contains(item.getQuality()))
                    .filter(item -> bindTypes.isEmpty() || bindTypes.contains(item.getBindType()))
                    .filter(item -> itemIds.isEmpty() || itemIds.contains(item.getId()))
                    .filter(item -> idFilterMin < 1 || item.getId() >= idFilterMin)
                    .filter(item -> idFilterMax < 1 || item.getId() <= idFilterMax)
                    .anyMatch(item -> nameFilter == null || nameFilter.matcher(item.getName()).matches());
        });
    }

    @Information(
            value = "item.craft",
            desc = "Listens for players that craft items.",
            conf = {
                    "recipe(String): unique displayName of the recipe",
                    "recipes(StringList): unique list of recipe names",
                    "displayName-filter(RegExPattern): regex for names"
            }
    )
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemCraft(CraftItemEvent event) {

        if (event.getInventory() instanceof FurnaceInventory) {
            return;
        }
        if (!CustomItemUtil.isCustomItem(event.getRecipe().getResult())) {
            return;
        }

        informListeners("craft", event.getWhoClicked(), config -> {

            CustomRecipe customRecipe = RaidCraft.getComponent(ItemsPlugin.class).getCraftingManager().getMatchingRecipe(event.getRecipe());
            if (config.isSet("recipe")) {
                return customRecipe.getName().equalsIgnoreCase(config.getString("recipe"));
            }
            if (config.isList("recipes")) {
                return config.getStringList("recipes").contains(customRecipe.getName().toLowerCase());
            }
            if (config.isSet("displayName-filter")) {
                return Pattern.matches(config.getString("displayName-filter"), customRecipe.getName());
            }
            return true;
        });
    }
}
