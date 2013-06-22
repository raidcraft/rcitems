package de.raidcraft.items.crafting;

import de.raidcraft.items.crafting.recipes.CustomRecipe;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

/**
 * @author Silthus
 */
public class CraftingListener implements Listener {

    private final CraftingManager craftingManager;

    protected CraftingListener(CraftingManager craftingManager) {

        this.craftingManager = craftingManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareCrafting(PrepareItemCraftEvent event) {

        if (!CustomItemUtil.isCustomItem(event.getRecipe().getResult())) {
            return;
        }
        CustomRecipe customRecipe = craftingManager.getMatchingRecipe(event.getRecipe());
        if (customRecipe == null) {
            event.getInventory().setResult(null);
            return;
        }
        if (!customRecipe.isMatchingRecipe(event.getInventory())) {
            event.getInventory().setResult(null);
        }
    }
}
