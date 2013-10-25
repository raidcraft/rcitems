package de.raidcraft.items.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.attachments.ItemAttachmentException;
import de.raidcraft.api.items.attachments.UseableCustomItem;
import de.raidcraft.api.items.tooltip.EquipmentTypeTooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            // lets check the durability loss and negate it by using our own durability if it is a custom item
            Player player = (Player) event.getEntity();
            ItemStack[] armorContents = player.getEquipment().getArmorContents();
            for (int i = 0; i < armorContents.length; i++) {
                ItemStack itemStack = plugin.updateItemDurability(player, armorContents[i], config.durabilityLossChanceOnDamage);
                armorContents[i] = itemStack;
            }
            player.getEquipment().setArmorContents(armorContents);
        }
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            player.getInventory().setItem(CustomItemUtil.MAIN_WEAPON_SLOT,
                    plugin.updateItemDurability(player, player.getInventory().getItem(CustomItemUtil.MAIN_WEAPON_SLOT), config.durabilityLossChanceOnUse));
            player.getInventory().setItem(CustomItemUtil.OFFHAND_WEAPON_SLOT,
                    plugin.updateItemDurability(player, player.getInventory().getItem(CustomItemUtil.OFFHAND_WEAPON_SLOT), config.durabilityLossChanceOnUse));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemPickup(PlayerPickupItemEvent event) {

        ItemStack itemStack = event.getItem().getItemStack();
        if (itemStack == null || itemStack.getTypeId() == 0) {
            return;
        }
        try {
            CustomItemStack customItemStack = rebuildCustomItem(event.getPlayer(), itemStack);
            if (customItemStack != null && customItemStack.hasTooltip(TooltipSlot.EQUIPMENT_TYPE)) {
                EquipmentTypeTooltip tooltip = (EquipmentTypeTooltip) customItemStack.getTooltip(TooltipSlot.EQUIPMENT_TYPE);
                tooltip.setColor(ChatColor.WHITE);
            }
            equipCustomWeapons(event.getPlayer());
        } catch (CustomItemException e) {
            int pickupSlot = CustomItemUtil.getPickupSlot(event);
            if (pickupSlot == CustomItemUtil.MAIN_WEAPON_SLOT || pickupSlot == CustomItemUtil.OFFHAND_WEAPON_SLOT) {
                CustomItemUtil.denyItem(event.getPlayer(), pickupSlot, itemStack, e.getMessage());
            }
        }
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

        rebuildInventory((Player) event.getPlayer());
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
                    contents[i] = stack;
                }
            } catch (CustomItemException e) {
                if (i == CustomItemUtil.MAIN_WEAPON_SLOT || i == CustomItemUtil.OFFHAND_WEAPON_SLOT) {
                    CustomItemUtil.denyItem(player, i, itemStack, e.getMessage());
                }
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

        equipCustomItem(player, CustomItemUtil.MAIN_WEAPON_SLOT, player.getInventory().getItem(CustomItemUtil.MAIN_WEAPON_SLOT));
        equipCustomItem(player, CustomItemUtil.OFFHAND_WEAPON_SLOT, player.getInventory().getItem(CustomItemUtil.OFFHAND_WEAPON_SLOT));
    }

    private void equipCustomItem(Player player, int slot, ItemStack itemStack) {

        CustomItemStack customItemStack = RaidCraft.getCustomItem(itemStack);

        if (customItemStack == null) {
            return;
        }
        try {
            customItemStack.rebuild(player);
            // lets check the requirements
            if (!customItemStack.getItem().isMeetingAllRequirements(player)) {
                CustomItemUtil.denyItem(player, slot, customItemStack, customItemStack.getItem().getResolveReason(player));
                return;
            }
            if (CustomItemUtil.isArmorSlot(slot)) {
                ItemStack[] armor = player.getInventory().getArmorContents();
                armor[slot - CustomItemUtil.ARMOR_SLOT] = customItemStack;
                player.getInventory().setArmorContents(armor);
            } else {
                player.getInventory().setItem(slot, customItemStack);
            }
        } catch (CustomItemException e) {
            CustomItemUtil.denyItem(player, slot, customItemStack, e.getMessage());
        }
    }

}
