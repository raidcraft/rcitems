package de.raidcraft.items;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.items.CustomItemManager;
import de.raidcraft.api.items.DuplicateCustomItemException;
import de.raidcraft.items.commands.ItemCommands;
import de.raidcraft.items.tables.TCustomEquipment;
import de.raidcraft.items.tables.TCustomItem;
import de.raidcraft.items.tables.TCustomWeapon;
import de.raidcraft.items.tables.TEquipmentAttribute;
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

        registerCommands(ItemCommands.class);
        // lets load all custom items that are defined in the database
        CustomItemManager component = RaidCraft.getComponent(CustomItemManager.class);
        Set<TCustomItem> customItems = getDatabase().find(TCustomItem.class).findSet();
        for (TCustomItem item : customItems) {
            if (item.getEquipment() != null) {
                if (item.getEquipment().getWeapon() != null) {
                    try {
                        ConfiguredWeapon weapon = new ConfiguredWeapon(item.getEquipment().getWeapon());
                        component.registerCustomItem(weapon);
                        loadedCustomItems.add(weapon.getId());
                        getLogger().info("loaded weapon: [" + weapon.getId() + "]" + weapon.getName());
                    } catch (DuplicateCustomItemException e) {
                        getLogger().warning(e.getMessage());
                    }
                }
                // TODO: load other item types here
            }
        }
    }

    @Override
    public void disable() {

        CustomItemManager component = RaidCraft.getComponent(CustomItemManager.class);
        // lets unload all of our custom items
        for (int id : loadedCustomItems) {
            component.unregisterCustomItem(id);
        }
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {

        ArrayList<Class<?>> tables = new ArrayList<>();
        tables.add(TCustomItem.class);
        tables.add(TCustomEquipment.class);
        tables.add(TEquipmentAttribute.class);
        tables.add(TCustomWeapon.class);
        return tables;
    }
}
