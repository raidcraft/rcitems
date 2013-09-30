package de.raidcraft.items.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.attachments.AttachableCustomItem;
import de.raidcraft.api.items.attachments.ItemAttachment;
import de.raidcraft.api.items.attachments.ItemAttachmentException;
import de.raidcraft.api.items.attachments.RequiredItemAttachment;
import de.raidcraft.api.items.attachments.UseableCustomItem;
import de.raidcraft.api.items.tooltip.RequirementTooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
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

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (CustomItemUtil.isCustomItem(event.getItem())) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                // lets check for a custom useable item
                CustomItemStack customItem = RaidCraft.getCustomItem(event.getItem());
                if (customItem == null) return;
                if (customItem.getItem() instanceof UseableCustomItem) {
                    try {
                        ((UseableCustomItem) customItem.getItem()).use(event.getPlayer(), customItem);
                        event.setCancelled(true);
                    } catch (ItemAttachmentException e) {
                        event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = false)
    public void useItemDurability(PlayerInteractEvent event) {

        if (CustomItemUtil.isCustomItem(event.getItem())) {
            // also update the item in hand
            plugin.updateItemDurability(event.getItem(), config.durabilityLossChanceOnUse);
        }
    }

    @EventHandler(ignoreCancelled = false)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        // lets check the durability loss and negate it by using our own durability if it is a custom item
        for (ItemStack itemStack : ((LivingEntity) event.getEntity()).getEquipment().getArmorContents()) {
            plugin.updateItemDurability(itemStack, config.durabilityLossChanceOnDamage);
        }
        if (event.getEntity() instanceof Player) {
            ((Player) event.getEntity()).updateInventory();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEnchant(EnchantItemEvent event) {

        ItemStack item = event.getItem();
        if (item != null && item.getTypeId() != 0 && CustomItemUtil.isCustomItem(item)) {
            RaidCraft.getCustomItem(item).rebuild(event.getEnchanter());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemPickup(PlayerPickupItemEvent event) {

        ItemStack itemStack = event.getItem().getItemStack();
        if (itemStack == null || itemStack.getTypeId() == 0) {
            return;
        }
        try {
            equipCustomItem(event.getPlayer(), itemStack);
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
        try {
            dropCustomItem(event.getPlayer(), itemStack);
        } catch (CustomItemException e) {
            event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHeldItemChange(PlayerItemHeldEvent event) {

        ItemStack itemStack = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (itemStack == null || itemStack.getTypeId() == 0) {
            return;
        }
        try {
            equipCustomItem(event.getPlayer(), itemStack);
        } catch (CustomItemException e) {
            event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {

        if (event.getPlayer() instanceof Player) {
            rebuildInventory((Player) event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {

        if (event.getPlayer() instanceof Player) {
            rebuildInventory((Player) event.getPlayer());
        }
    }

    private void rebuildInventory(Player player) {

        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack == null || itemStack.getTypeId() == 0) {
                continue;
            }
            try {
                CustomItemStack stack = rebuildCustomItem(player, itemStack);
                if (stack != null) {
                    contents[i] = stack;
                }
            } catch (CustomItemException e) {
                player.sendMessage(ChatColor.RED + e.getMessage());
            }
        }
        player.getInventory().setContents(contents);
    }


    private CustomItemStack rebuildCustomItem(Player player, ItemStack itemStack) throws CustomItemException {

        CustomItemStack customItemStack = null;

        if (!CustomItemUtil.isCustomItem(itemStack) && config.getDefaultCustomItem(itemStack.getTypeId()) != 0) {
            customItemStack = RaidCraft.getCustomItem(config.getDefaultCustomItem(itemStack.getTypeId())).createNewItem();
        } else if (CustomItemUtil.isCustomItem(itemStack)) {
            customItemStack = RaidCraft.getCustomItem(itemStack);
            if (customItemStack == null) return null;
            customItemStack.rebuild(player);
        }

        return customItemStack;
    }

    private void equipCustomItem(Player player, ItemStack itemStack) throws CustomItemException {

        CustomItemStack customItemStack = rebuildCustomItem(player, itemStack);
        if (customItemStack == null) {
            return;
        }

        CustomItem customItem = customItemStack.getItem();

        if (customItem != null && customItem instanceof AttachableCustomItem) {
            ((AttachableCustomItem) customItem).apply(player, customItemStack);
            // lets also add our requirement lore
            for (ItemAttachment attachment : ((AttachableCustomItem) customItem).getAttachments(player)) {
                if (attachment instanceof RequiredItemAttachment) {
                    ChatColor color = ChatColor.WHITE;

                    if (!((RequiredItemAttachment) attachment).isRequirementMet(player)) {
                        color = ChatColor.RED;
                    }
                    if (!customItemStack.hasTooltip(TooltipSlot.REQUIREMENT)) {
                        customItemStack.setTooltip(new RequirementTooltip(((RequiredItemAttachment) attachment).getName(),
                                color + ((RequiredItemAttachment) attachment).getItemText(player)));
                    } else {
                        RequirementTooltip tooltip = (RequirementTooltip) customItemStack.getTooltip(TooltipSlot.REQUIREMENT);
                        tooltip.addRequirement(((RequiredItemAttachment) attachment).getName(),
                                color + ((RequiredItemAttachment) attachment).getItemText(player));
                    }
                }
            }
        }
        customItemStack.rebuild(player);
        rebuildInventory(player);
    }

    private void dropCustomItem(Player player, ItemStack itemStack) throws CustomItemException {

        CustomItemStack customItemStack = rebuildCustomItem(player, itemStack);
        if (customItemStack == null) {
            return;
        }

        CustomItem customItem = customItemStack.getItem();

        if (customItem != null && customItem instanceof AttachableCustomItem) {
            ((AttachableCustomItem) customItem).remove(player, customItemStack);
        }
        customItemStack.rebuild();
        rebuildInventory(player);
    }
}
