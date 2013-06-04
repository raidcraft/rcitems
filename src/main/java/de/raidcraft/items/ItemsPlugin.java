package de.raidcraft.items;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.items.CustomItemManager;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.DuplicateCustomItemException;
import de.raidcraft.items.commands.ItemCommands;
import de.raidcraft.items.tables.TCustomArmor;
import de.raidcraft.items.tables.TCustomEquipment;
import de.raidcraft.items.tables.TCustomItem;
import de.raidcraft.items.tables.TCustomWeapon;
import de.raidcraft.items.tables.TEquipmentAttribute;
import de.raidcraft.items.weapons.ConfiguredArmor;
import de.raidcraft.items.weapons.ConfiguredWeapon;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Silthus
 */
public class ItemsPlugin extends BasePlugin implements Listener {

    private final Set<Integer> loadedCustomItems = new HashSet<>();
    private LocalConfiguration config;

    @Override
    public void enable() {

        config = configure(new LocalConfiguration(this), true);
        registerEvents(this);
        registerCommands(ItemCommands.class);
        loadCustomItems();
    }

    @Override
    public void disable() {

        // lets unload all of our custom items
        unloadRegisteredItems();
    }

    private void unloadRegisteredItems() {

        CustomItemManager component = RaidCraft.getComponent(CustomItemManager.class);
        for (int id : loadedCustomItems) {
            component.unregisterCustomItem(id);
        }
        loadedCustomItems.clear();
    }

    @Override
    public void reload() {

        unloadRegisteredItems();
        loadCustomItems();
    }

    private void loadCustomItems() {

        // lets load all custom items that are defined in the database
        CustomItemManager component = RaidCraft.getComponent(CustomItemManager.class);
        Set<TCustomItem> customItems = getDatabase().find(TCustomItem.class).findSet();
        for (TCustomItem item : customItems) {

            TCustomEquipment equipment = getDatabase().find(TCustomEquipment.class).where().eq("item_id", item.getId()).findUnique();
            if (equipment == null) continue;

            try {
                switch (item.getItemType()) {

                    case WEAPON:
                        TCustomWeapon weapon = getDatabase().find(TCustomWeapon.class).where().eq("equipment_id", equipment.getId()).findUnique();
                        if (weapon == null) continue;
                        ConfiguredWeapon configuredWeapon = new ConfiguredWeapon(weapon);
                        component.registerCustomItem(configuredWeapon);
                        loadedCustomItems.add(configuredWeapon.getId());
                        getLogger().info("loaded weapon: [" + configuredWeapon.getId() + "]" + configuredWeapon.getName());
                        break;
                    case ARMOR:
                        TCustomArmor armor = getDatabase().find(TCustomArmor.class).where().eq("equipment_id", equipment.getId()).findUnique();
                        if (armor == null) continue;
                        ConfiguredArmor configuredArmor = new ConfiguredArmor(armor);
                        component.registerCustomItem(configuredArmor);
                        loadedCustomItems.add(configuredArmor.getId());
                        getLogger().info("loaded armor: [" + configuredArmor.getId() + "]" + configuredArmor.getName());
                        break;
                }
            } catch (DuplicateCustomItemException e) {
                getLogger().warning(e.getMessage());
            }
        }
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {

        ArrayList<Class<?>> tables = new ArrayList<>();
        tables.add(TCustomItem.class);
        tables.add(TCustomEquipment.class);
        tables.add(TEquipmentAttribute.class);
        tables.add(TCustomWeapon.class);
        tables.add(TCustomArmor.class);
        return tables;
    }

    private void applyDurabilityLoss(ItemStack item, double chance) {

        // lets check the durability loss and negate it by using our own durability if it is a custom item
        if (!CustomItemUtil.isEquipment(item)) {
            return;
        }
        // on each interact with the item the player has a chance of 0.1% chance to loose one durability point
        if (Math.random() < chance) {
            CustomItemStack customItem = RaidCraft.getCustomItem(item);
            customItem.setDurability(customItem.getDurability() - 1);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {

        // lets check the durability loss and negate it by using our own durability if it is a custom item
        applyDurabilityLoss(event.getItem(), config.durabilityLossChanceOnUse);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        // lets check the durability loss and negate it by using our own durability if it is a custom item
        for (ItemStack itemStack : ((LivingEntity) event.getEntity()).getEquipment().getArmorContents()) {
            applyDurabilityLoss(itemStack, config.durabilityLossChanceOnDamage);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEnchant(EnchantItemEvent event) {

        ItemStack item = event.getItem();
        if (item != null && item.getTypeId() != 0 && CustomItemUtil.isCustomItem(item)) {
            RaidCraft.getCustomItem(item).rebuild();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
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

    public static class LocalConfiguration extends ConfigurationBase<ItemsPlugin> {

        public LocalConfiguration(ItemsPlugin plugin) {

            super(plugin, "config.yml");
        }

        @Setting("durability-loss-chance-on-use")
        public double durabilityLossChanceOnUse = 0.001;
        @Setting("durability-loss-chance-on-damage")
        public double durabilityLossChanceOnDamage = 0.0001;

        public int getDefaultCustomItem(int minecraftId) {

            if (!isSet("defaults." + minecraftId)) {
                return 0;
            }
            return getInt("defaults." + minecraftId, 0);
        }
    }

}
