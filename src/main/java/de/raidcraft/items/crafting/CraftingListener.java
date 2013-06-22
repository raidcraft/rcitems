package de.raidcraft.items.crafting;

import de.raidcraft.items.crafting.recipes.CustomRecipe;
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

        if (!(event.getRecipe() instanceof CustomRecipe)) {
            return;
        }
        CustomRecipe recipe = craftingManager.getMatchingRecipe(event.getRecipe());
        if (recipe == null) {
            return;
        }
        // lets now check if the input are actual the specified custom items
        if (!recipe.isMatchingRecipe(event.getInventory())) {
            // its not our custom crafting recipe so lets set the result to null
            event.getInventory().setResult(null);
        }
    }
}
