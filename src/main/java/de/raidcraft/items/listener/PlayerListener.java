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
        equipCustomWeapons(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent event) {

        rebuildInventory(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHeldItemChange(PlayerItemHeldEvent event) {

        ItemStack itemStack = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (itemStack == null || itemStack.getTypeId() == 0) {
            return;
        }
        equipCustomWeapons(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {

        if (event.getPlayer() instanceof Player) {
            equipCustomWeapons((Player) event.getPlayer());
            equipCustomArmor((Player) event.getPlayer());
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
                    if (stack.getItem() instanceof AttachableCustomItem) {
                        buildAttachmentInfo(player, (AttachableCustomItem) stack.getItem(), stack);
                    }
                    contents[i] = stack;
                }
            } catch (CustomItemException e) {
                player.sendMessage(ChatColor.RED + e.getMessage());
            }
        }
        player.getInventory().setContents(contents);
    }


    private CustomItemStack rebuildCustomItem(Player player, ItemStack itemStack) throws CustomItemException {

        if (itemStack == null) {
            return null;
        }
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

    private void equipCustomArmor(Player player) {

        ItemStack[] armorContents = player.getEquipment().getArmorContents();
        for (int i = 0; i < armorContents.length; i++) {
            equipCustomItem(player, i + CustomItemUtil.ARMOR_SLOT, armorContents[i]);
        }
    }

    private void equipCustomWeapons(Player player) {

        equipCustomItem(player, 0, player.getInventory().getItem(0));
        equipCustomItem(player, 1, player.getInventory().getItem(1));
    }

    private void equipCustomItem(Player player, int slot, ItemStack itemStack) {

        CustomItemStack customItemStack = RaidCraft.getCustomItem(itemStack);

        if (customItemStack == null) {
            return;
        }
        try {
            CustomItem customItem = customItemStack.getItem();
            if (customItem != null && customItem instanceof AttachableCustomItem) {
                ((AttachableCustomItem) customItem).apply(player, customItemStack);
                buildAttachmentInfo(player, (AttachableCustomItem) customItem, customItemStack);
            }
            customItemStack.rebuild(player);
            if (CustomItemUtil.isArmorSlot(slot)) {
                ItemStack[] armor = player.getInventory().getArmorContents();
                armor[slot] = customItemStack;
                player.getInventory().setArmorContents(armor);
            } else {
                player.getInventory().setItem(slot, customItemStack);
            }
        } catch (CustomItemException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
            if (CustomItemUtil.isArmorSlot(slot)) {
                CustomItemUtil.moveArmor(player, slot - CustomItemUtil.ARMOR_SLOT, itemStack);
            } else {
                CustomItemUtil.moveItem(player, slot, itemStack);
            }
        }
    }

    private void buildAttachmentInfo(Player player, AttachableCustomItem item, CustomItemStack stack) throws CustomItemException {

        // lets also add our requirement lore
        for (ItemAttachment attachment : item.getAttachments(player)) {
            if (attachment instanceof RequiredItemAttachment) {
                if (stack.hasTooltip(TooltipSlot.REQUIREMENT)) {
                    RequirementTooltip tooltip = (RequirementTooltip) stack.getTooltip(TooltipSlot.REQUIREMENT);
                    tooltip.addRequirement((RequiredItemAttachment) attachment);
                } else {
                    stack.setTooltip(new RequirementTooltip((RequiredItemAttachment) attachment));
                }
            }
        }
    }
}
