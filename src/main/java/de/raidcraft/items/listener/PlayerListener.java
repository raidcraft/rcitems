package de.raidcraft.items.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.InventorySlot;
import de.raidcraft.api.items.ItemBindType;
import de.raidcraft.api.items.attachments.ItemAttachmentException;
import de.raidcraft.api.items.attachments.UseableCustomItem;
import de.raidcraft.api.items.events.EquipCustomArmorEvent;
import de.raidcraft.api.items.events.EquipCustomItemEvent;
import de.raidcraft.api.items.events.EquipCustomWeaponEvent;
import de.raidcraft.api.items.tooltip.EquipmentTypeTooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.TimeUtil;
import de.raidcraft.util.UUIDUtil;
import de.raidcraft.util.fanciful.FancyMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Silthus
 */
public class PlayerListener implements Listener {

    private final ItemsPlugin plugin;
    private final ItemsPlugin.LocalConfiguration config;
    @Getter
    private static final Map<UUID, List<CustomItemStack>> autoCompleteItems = new HashMap<>();
    private static final Map<UUID, List<Item>> warnedPickupItems = new HashMap<>();

    public PlayerListener(ItemsPlugin plugin) {

        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void sendItemText(InventoryClickEvent event) {

        if (event.getClick() == ClickType.MIDDLE && event.getWhoClicked() instanceof Player) {
            CustomItemStack customItem = RaidCraft.getCustomItem(event.getCurrentItem());
            if (customItem != null) {
                new FancyMessage("Nutze während dem Chatten ?[Tab] um alle Items die du " +
                        "mit Mittelklick angeklickt hast zu vervollständigen. Folgendes Item wurde hinzugefügt: ")
                        .color(ChatColor.YELLOW)
                        .then("[" + customItem.getItem().getName() + "]")
                        .color(customItem.getItem().getQuality().getColor())
                        .itemTooltip(customItem)
                        .send((Player) event.getWhoClicked());
                UUID uniqueId = event.getWhoClicked().getUniqueId();
                if (!autoCompleteItems.containsKey(uniqueId)) {
                    autoCompleteItems.put(uniqueId, new ArrayList<>());
                }
                autoCompleteItems.get(uniqueId).add(customItem);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {

        if (CustomItemUtil.isCustomItem(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getPlayer().hasMetadata("NPC")) return;
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
                } else if (customItem.getItem().isBlockingUsage()) {
                    event.setCancelled(true);
                }
            } else {
                updateWeaponDurability(event.getPlayer());
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {

        if (event.getEntity().hasMetadata("NPC")) return;
        if (event.getEntity() instanceof Player) {
            // lets check the durability loss and negate it by using our own durability if it is a custom item
            Player player = (Player) event.getEntity();
            ItemStack[] armorContents = player.getEquipment().getArmorContents();
            for (int i = 0; i < armorContents.length; i++) {
                ItemStack itemStack = plugin.updateItemDurability(player, armorContents[i], config.durabilityLossChanceOnDamage);
                armorContents[i] = itemStack;
            }
            player.getEquipment().setArmorContents(armorContents);
        } else if (event.getDamager() instanceof Player) {
            updateWeaponDurability((Player) event.getDamager());
            ((Player) event.getDamager()).updateInventory(); // we have to update the player inventory to make changes visible
        }
    }

    private void updateWeaponDurability(Player player) {

        player.getInventory().setItem(CustomItemUtil.MAIN_WEAPON_SLOT,
                plugin.updateItemDurability(player, player.getInventory().getItem(CustomItemUtil.MAIN_WEAPON_SLOT), config.durabilityLossChanceOnUse));
        player.getInventory().setItem(CustomItemUtil.OFFHAND_WEAPON_SLOT,
                plugin.updateItemDurability(player, player.getInventory().getItem(CustomItemUtil.OFFHAND_WEAPON_SLOT), config.durabilityLossChanceOnUse));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemPickup(PlayerPickupItemEvent event) {

        if (event.getPlayer().hasMetadata("NPC")) return;
        ItemStack itemStack = event.getItem().getItemStack();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        try {
            CustomItemStack customItemStack = rebuildCustomItem(event.getPlayer(), itemStack);
            if (customItemStack != null) {
                if (customItemStack.hasTooltip(TooltipSlot.EQUIPMENT_TYPE)) {
                    EquipmentTypeTooltip tooltip = (EquipmentTypeTooltip) customItemStack.getTooltip(TooltipSlot.EQUIPMENT_TYPE);
                    tooltip.setColor(ChatColor.WHITE);
                }
                Optional<UUID> owner = customItemStack.getOwner();
                if (owner.isPresent() && !owner.get().equals(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                    if (warnedPickupItems.getOrDefault(event.getPlayer().getUniqueId(), new ArrayList<>()).contains(event.getItem())) {
                        return;
                    }
                    FancyMessage message = CustomItemUtil.getFormattedItemTooltip(new FancyMessage(""), customItemStack);
                    message.then(" ist an ").color(ChatColor.RED).then(UUIDUtil.getNameFromUUID(owner.get())).color(ChatColor.AQUA)
                            .then(" gebunden und kann nicht aufgenommen werden!").color(ChatColor.RED)
                            .send(event.getPlayer());
                    if (!warnedPickupItems.containsKey(event.getPlayer().getUniqueId())) {
                        warnedPickupItems.put(event.getPlayer().getUniqueId(), new ArrayList<>());
                    }
                    warnedPickupItems.get(event.getPlayer().getUniqueId()).add(event.getItem());
                    Bukkit.getScheduler().runTaskLater(plugin,
                            () -> warnedPickupItems.getOrDefault(event.getPlayer().getUniqueId(), new ArrayList<>()).remove(event.getItem()),
                            TimeUtil.secondsToTicks(plugin.getConfig().soulboundItemPickupWarnInterval));
                    return;
                }
                if (customItemStack.getItem().getBindType() == ItemBindType.BIND_ON_PICKUP) {
                    customItemStack.setOwner(event.getPlayer());
                }
                event.getItem().setItemStack(customItemStack);
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

        if (event.getPlayer().hasMetadata("NPC")) return;
        rebuildInventory(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHeldItemChange(PlayerItemHeldEvent event) {

        if (event.getPlayer().hasMetadata("NPC")) return;
        ItemStack itemStack = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (itemStack == null || itemStack.getTypeId() == 0) {
            return;
        }
        equipCustomWeapons(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {

        if (event.getPlayer().hasMetadata("NPC")) return;
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
            Optional<UUID> owner = customItemStack.getOwner();
            if (owner.isPresent() && !owner.get().equals(player.getUniqueId())) {
                CustomItemUtil.denyItem(player, slot, customItemStack, plugin.getTranslationProvider().tr(player, "itemequip.deny-soulbound",
                        ChatColor.RED + "Das Item %s ist an %s gebunden und kann nicht angelegt werden!",
                        customItemStack.getItemMeta().getDisplayName(), UUIDUtil.getNameFromUUID(owner.get())));
                return;
            }
            // bind the item if it is bind on equip
            if (customItemStack.getItem().getBindType() == ItemBindType.BIND_ON_EQUIP) {
                customItemStack.setOwner(player);
            }
            EquipCustomItemEvent event;
            if (CustomItemUtil.isArmor(customItemStack)) {
                event = new EquipCustomArmorEvent(player, CustomItemUtil.getArmor(customItemStack), InventorySlot.fromSlot(slot));
            } else if (CustomItemUtil.isWeapon(customItemStack)) {
                event = new EquipCustomWeaponEvent(player, CustomItemUtil.getWeapon(customItemStack), InventorySlot.fromSlot(slot));
            } else {
                event = new EquipCustomItemEvent(player, customItemStack.getItem(), InventorySlot.fromSlot(slot));
            }
            RaidCraft.callEvent(event);
            if (event.isCancelled()) {
                CustomItemUtil.denyItem(player, slot, customItemStack, "Item kann nicht angelegt werden.");
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
