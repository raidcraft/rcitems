package de.raidcraft.items;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.items.CustomItemManager;
import de.raidcraft.api.items.DuplicateCustomItemException;
import de.raidcraft.items.commands.ItemCommands;
import de.raidcraft.items.tables.TCustomArmor;
import de.raidcraft.items.tables.TCustomEquipment;
import de.raidcraft.items.tables.TCustomItem;
import de.raidcraft.items.tables.TCustomWeapon;
import de.raidcraft.items.tables.TEquipmentAttribute;
import de.raidcraft.items.util.Font;
import de.raidcraft.items.weapons.ConfiguredArmor;
import de.raidcraft.items.weapons.ConfiguredWeapon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Silthus
 */
public class ItemsPlugin extends BasePlugin {

    private final Set<Integer> loadedCustomItems = new HashSet<>();

    @Override
    public void enable() {

        Font.init();
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
}
