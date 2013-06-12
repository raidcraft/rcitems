package de.raidcraft.items;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemManager;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.DuplicateCustomItemException;
import de.raidcraft.api.items.attachments.AttachableCustomItem;
import de.raidcraft.items.commands.ItemCommands;
import de.raidcraft.items.configs.AttachmentConfig;
import de.raidcraft.items.equipment.ConfiguredArmor;
import de.raidcraft.items.equipment.ConfiguredWeapon;
import de.raidcraft.items.listener.PlayerListener;
import de.raidcraft.items.tables.TCustomArmor;
import de.raidcraft.items.tables.TCustomEquipment;
import de.raidcraft.items.tables.TCustomItem;
import de.raidcraft.items.tables.TCustomItemAttachment;
import de.raidcraft.items.tables.TCustomWeapon;
import de.raidcraft.items.tables.TEquipmentAttribute;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.StringUtils;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Silthus
 */
public class ItemsPlugin extends BasePlugin {

    private final Set<Integer> loadedCustomItems = new HashSet<>();
    private final Map<String, AttachmentConfig> loadedAttachments = new HashMap<>();
    private LocalConfiguration config;

    @Override
    public void enable() {

        config = configure(new LocalConfiguration(this), true);
        registerEvents(new PlayerListener(this));
        registerCommands(ItemCommands.class);
        // the attachments need to load before the custom items because we use them in there
        loadAttachments();
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
        // the attachments need to load before the custom items because we use them in there
        loadAttachments();
        loadCustomItems();
    }

    public LocalConfiguration getConfig() {

        return config;
    }

    private void loadAttachments() {

        loadedAttachments.clear();
        File dir = new File(getDataFolder(), "attachments");
        dir.mkdirs();
        loadAttachments(dir);
    }

    private void loadAttachments(File dir) {

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                loadAttachments(file);
            }
            if (!file.getName().endsWith(".yml")) {
                continue;
            }
            AttachmentConfig config = configure(new AttachmentConfig(this, file), false);
            loadedAttachments.put(config.getName(), config);
            getLogger().info("Loaded item attachment config: " + config.getName());
        }
    }

    private void loadCustomItems() {

        loadedCustomItems.clear();
        // lets load all custom items that are defined in the database
        CustomItemManager component = RaidCraft.getComponent(CustomItemManager.class);
        Set<TCustomItem> customItems = getDatabase().find(TCustomItem.class).findSet();
        for (TCustomItem item : customItems) {

            TCustomEquipment equipment = getDatabase().find(TCustomEquipment.class).where().eq("item_id", item.getId()).findUnique();
            if (equipment == null) continue;

            try {
                CustomItem customItem;
                switch (item.getItemType()) {

                    case WEAPON:
                        TCustomWeapon weapon = getDatabase().find(TCustomWeapon.class).where().eq("equipment_id", equipment.getId()).findUnique();
                        if (weapon == null) continue;
                        customItem = new ConfiguredWeapon(weapon);
                        break;
                    case ARMOR:
                        TCustomArmor armor = getDatabase().find(TCustomArmor.class).where().eq("equipment_id", equipment.getId()).findUnique();
                        if (armor == null) continue;
                        customItem = new ConfiguredArmor(armor);
                        break;
                    default:
                        continue;
                }
                // lets check for custom item attachments
                if (item.getAttachments() != null && !item.getAttachments().isEmpty()) {
                    for (TCustomItemAttachment attachment : item.getAttachments()) {
                        String attachmentName = StringUtils.formatName(attachment.getAttachmentName());
                        if (!loadedAttachments.containsKey(attachmentName)) {
                            getLogger().warning("Unknown item attachment defined in the config of custom item with id " + item.getId());
                            continue;
                        }
                        AttachmentConfig config = loadedAttachments.get(attachmentName);
                        ((AttachableCustomItem) customItem).addAttachment(config.getName(), config);
                    }
                }
                // register the actual custom item
                component.registerCustomItem(customItem);
                loadedCustomItems.add(customItem.getId());
                getLogger().info("loaded item: [" + customItem.getId() + "]" + customItem.getName());
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
        tables.add(TCustomItemAttachment.class);
        return tables;
    }

    public void applyDurabilityLoss(ItemStack item, double chance) {

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
