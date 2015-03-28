package de.raidcraft.items.crafting;

import de.raidcraft.RaidCraft;
import de.raidcraft.items.crafting.recipes.CustomFurnaceRecipe;
import de.raidcraft.items.crafting.recipes.CustomRecipe;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.FurnaceInventory;

/**
 * @author Silthus
 */
public class CraftingListener implements Listener {

    private final CraftingManager craftingManager;

    protected CraftingListener(CraftingManager craftingManager) {

        this.craftingManager = craftingManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnaceUse(FurnaceSmeltEvent event) {

        CustomFurnaceRecipe recipe = craftingManager.getFurnaceRecipe(event.getSource());
        if (recipe == null) {
            return;
        }
        if (!recipe.isMatchingRecipe(event.getSource())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getInventory() instanceof FurnaceInventory)) {
            return;
        }
        if (!ItemUtils.isStackValid(((FurnaceInventory) event.getInventory()).getSmelting())) {
            return;
        }
        CustomFurnaceRecipe recipe = craftingManager.getFurnaceRecipe(((FurnaceInventory) event.getInventory()).getSmelting());
        if (recipe == null) {
            return;
        }
        if (recipe.getPermission() != null && !event.getWhoClicked().hasPermission(recipe.getPermission())) {
            if (event.getWhoClicked() instanceof Player) {
                ((Player) event.getWhoClicked()).sendMessage(
                        ChatColor.RED + "Du bist noch nicht erfahren genug um dieses Gegenstand einzuschmelzen.");
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareCrafting(PrepareItemCraftEvent event) {

        if (event.getInventory() instanceof FurnaceInventory) {
            return;
        }
        if (!CustomItemUtil.isCustomItem(event.getRecipe().getResult())) {
            return;
        }
        CustomRecipe customRecipe = craftingManager.getMatchingRecipe(event.getRecipe());
        // in the first step we compare the two recipes with the basic minecraft items
        // if the minecraft items are not equal we cancel the crafting process
        if (customRecipe == null) {
            RaidCraft.LOGGER.info("No custom recipe found! Equal check failed");
            event.getInventory().setResult(null);
            return;
        }
        // in the second step we check the exact custom items against the inventory
        // this needs to happen later because recipes do not save itemstacks
        if (!customRecipe.isMatchingRecipe(event.getInventory())) {
            RaidCraft.LOGGER.info("Recipes are not equal");
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemCraftEvent(CraftItemEvent event) {

        if (event.getInventory() instanceof FurnaceInventory) {
            return;
        }
        if (!CustomItemUtil.isCustomItem(event.getRecipe().getResult())) {
            return;
        }
        CustomRecipe customRecipe = craftingManager.getMatchingRecipe(event.getRecipe());
        if (customRecipe != null) {
            if (customRecipe.getPermission() != null && !event.getWhoClicked().hasPermission(customRecipe.getPermission())) {
                if (event.getWhoClicked() instanceof Player) {
                    ((Player) event.getWhoClicked()).sendMessage(
                            ChatColor.RED + "Du verfügst noch nicht über genug Wissen um diesen Gegenstand herzustellen.");
                }
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }
}
