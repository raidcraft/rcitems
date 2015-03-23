package de.raidcraft.items.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.AttributeHolder;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.ItemAttribute;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.api.items.tooltip.EnchantmentTooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.items.ItemsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;

/**
 * @author mdoering
 */
public class EnchantmentListener implements Listener {

    private final ItemsPlugin plugin;

    public EnchantmentListener(ItemsPlugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onItemClick(InventoryClickEvent event) {

        CustomItemStack enchantment = RaidCraft.getCustomItem(event.getCursor());
        if (enchantment == null || enchantment.getItem().getType() != ItemType.ENCHANTMENT) {
            return;
        }
        CustomItemStack clickedItem = RaidCraft.getCustomItem(event.getCurrentItem());
        if (clickedItem == null) {
            return;
        }
        if (clickedItem.hasTooltip(TooltipSlot.ENCHANTMENTS) && !event.isShiftClick()) {
            ((Player) event.getWhoClicked()).sendMessage(ChatColor.RED +
                    "Das Item ist bereits verzaubert. Halte Shift gedrückt um es trotzdem zu verzaubern.");
            event.setCancelled(true);
            return;
        }
        if (enchantment.getItem() instanceof AttributeHolder) {
            try {
                Collection<ItemAttribute> attributes = ((AttributeHolder) enchantment.getItem()).getAttributes();
                clickedItem.setTooltip(new EnchantmentTooltip(attributes.toArray(new ItemAttribute[attributes.size()])));
                clickedItem.rebuild((Player) event.getWhoClicked());
                event.setCursor(clickedItem);
                event.setCurrentItem(null);
                event.setCancelled(true);
            } catch (CustomItemException e) {
                e.printStackTrace();
            }
        }
    }
}
