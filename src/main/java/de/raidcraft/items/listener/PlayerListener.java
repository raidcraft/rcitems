package de.raidcraft.items.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.attachments.AttachableCustomItem;
import de.raidcraft.api.items.attachments.ItemAttachmentException;
import de.raidcraft.api.items.attachments.UseableCustomItem;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Silthus
 */
public class PlayerListener implements Listener {

    private final ItemsPlugin plugin;
    private final ItemsPlugin.LocalConfiguration config;

    public PlayerListener(ItemsPlugin plugin) {

        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (CustomItemUtil.isCustomItem(event.getItem())) {
            // lets check the durability loss and negate it by using our own durability if it is a custom item
            plugin.applyDurabilityLoss(event.getItem(), config.durabilityLossChanceOnUse);
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                // lets check for a custom useable item
                CustomItemStack customItem = RaidCraft.getCustomItem(event.getItem());
                if (customItem.getItem() instanceof UseableCustomItem) {
                    try {
                        ((UseableCustomItem) customItem.getItem()).use(event.getPlayer());
                    } catch (ItemAttachmentException e) {
                        event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        // lets check the durability loss and negate it by using our own durability if it is a custom item
        for (ItemStack itemStack : ((LivingEntity) event.getEntity()).getEquipment().getArmorContents()) {
            plugin.applyDurabilityLoss(itemStack, config.durabilityLossChanceOnDamage);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {

        ItemStack item = event.getItem();
        if (item != null && item.getTypeId() != 0 && CustomItemUtil.isCustomItem(item)) {
            RaidCraft.getCustomItem(item).rebuild();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemPickup(PlayerPickupItemEvent event) {

        ItemStack itemStack = event.getItem().getItemStack();
        if (itemStack == null || itemStack.getTypeId() == 0) {
            return;
        }
        try {
            CustomItem customItem = null;
            if (!CustomItemUtil.isCustomItem(itemStack) && config.getDefaultCustomItem(itemStack.getTypeId()) != 0) {
                customItem = RaidCraft.getCustomItem(config.getDefaultCustomItem(itemStack.getTypeId()));
                customItem.rebuild(itemStack);
            } else if (CustomItemUtil.isCustomItem(itemStack)) {
                CustomItemStack customItemStack = RaidCraft.getCustomItem(itemStack);
                customItemStack.rebuild();
                customItem = customItemStack.getItem();
            }
            if (customItem != null && customItem instanceof AttachableCustomItem) {
                try {
                    ((AttachableCustomItem) customItem).apply(event.getPlayer());
                } catch (CustomItemException e) {
                    event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
                    event.setCancelled(true);
                }
            }
        } catch (CustomItemException e) {
            event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent event) {

        ItemStack itemStack = event.getItemDrop().getItemStack();
        if (itemStack == null || itemStack.getTypeId() == 0) {
            return;
        }
        if (CustomItemUtil.isCustomItem(itemStack)) {
            CustomItemStack customItem = RaidCraft.getCustomItem(itemStack);
            if (customItem.getItem() instanceof AttachableCustomItem) {
                try {
                    ((AttachableCustomItem) customItem.getItem()).remove(event.getPlayer());
                } catch (CustomItemException e) {
                    event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeldItemChange(PlayerItemHeldEvent event) {

        ItemStack itemStack = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (itemStack == null || itemStack.getTypeId() == 0) {
            return;
        }
        try {
            if (!CustomItemUtil.isCustomItem(itemStack) && config.getDefaultCustomItem(itemStack.getTypeId()) != 0) {
                RaidCraft.getCustomItem(config.getDefaultCustomItem(itemStack.getTypeId())).rebuild(itemStack);
            } else if (CustomItemUtil.isCustomItem(itemStack)) {
                CustomItemStack customItem = RaidCraft.getCustomItem(itemStack);
                if (customItem == null) return;
                customItem.rebuild();
            }
        } catch (CustomItemException e) {
            event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {

        for (ItemStack itemStack : event.getPlayer().getInventory().getContents()) {
            if (itemStack == null || itemStack.getTypeId() == 0) {
                continue;
            }
            try {
                if (!CustomItemUtil.isCustomItem(itemStack) && config.getDefaultCustomItem(itemStack.getTypeId()) != 0) {
                    RaidCraft.getCustomItem(config.getDefaultCustomItem(itemStack.getTypeId())).rebuild(itemStack);
                } else if (CustomItemUtil.isCustomItem(itemStack)) {
                    RaidCraft.getCustomItem(itemStack).rebuild();
                }
            } catch (CustomItemException ignored) {

            }
        }
    }
}
