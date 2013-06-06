package de.raidcraft.items.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Silthus
 */
public class PlayerListener {

    private final ItemsPlugin plugin;
    private final ItemsPlugin.LocalConfiguration config;

    public PlayerListener(ItemsPlugin plugin) {

        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        // lets check the durability loss and negate it by using our own durability if it is a custom item
        plugin.applyDurabilityLoss(event.getItem(), config.durabilityLossChanceOnUse);
    }

    @EventHandler(ignoreCancelled = true)
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

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {

        ItemStack itemStack = event.getItem().getItemStack();
        if (itemStack == null || itemStack.getTypeId() == 0) {
            return;
        }
        if (!CustomItemUtil.isCustomItem(itemStack) && config.getDefaultCustomItem(itemStack.getTypeId()) != 0) {
            RaidCraft.getCustomItem(config.getDefaultCustomItem(itemStack.getTypeId())).rebuild(itemStack);
        } else if (CustomItemUtil.isCustomItem(itemStack)) {
            RaidCraft.getCustomItem(itemStack).rebuild();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeldItemChange(PlayerItemHeldEvent event) {

        ItemStack itemStack = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (itemStack == null || itemStack.getTypeId() == 0) {
            return;
        }
        if (!CustomItemUtil.isCustomItem(itemStack) && config.getDefaultCustomItem(itemStack.getTypeId()) != 0) {
            RaidCraft.getCustomItem(config.getDefaultCustomItem(itemStack.getTypeId())).rebuild(itemStack);
        } else if (CustomItemUtil.isCustomItem(itemStack)) {
            RaidCraft.getCustomItem(itemStack).rebuild();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {

        for (ItemStack itemStack : event.getPlayer().getInventory().getContents()) {
            if (itemStack == null || itemStack.getTypeId() == 0) {
                continue;
            }
            if (!CustomItemUtil.isCustomItem(itemStack) && config.getDefaultCustomItem(itemStack.getTypeId()) != 0) {
                RaidCraft.getCustomItem(config.getDefaultCustomItem(itemStack.getTypeId())).rebuild(itemStack);
            } else if (CustomItemUtil.isCustomItem(itemStack)) {
                RaidCraft.getCustomItem(itemStack).rebuild();
            }
        }
    }
}
