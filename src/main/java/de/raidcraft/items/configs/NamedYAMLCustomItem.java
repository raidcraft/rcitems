package de.raidcraft.items.configs;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.ItemBindType;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.items.AbstractCustomItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class NamedYAMLCustomItem extends AbstractCustomItem {

    public NamedYAMLCustomItem(String name, ConfigurationSection config) throws CustomItemException {

        super(CustomItem.NAMED_CUSTOM_ITEM_ID, name, ItemType.fromString(config.getString("type", "Undefined")));
        Material item = Material.matchMaterial(config.getString("item"));
        if (item == null) throw new CustomItemException("Item Type " + config.getString("item") + " in " + name + " is invalid!");
        setMinecraftItem(item);
        short dataValue = (short) config.getInt("item-data", 0);
        if (dataValue > 0) setMinecraftDataValue(dataValue);
        if (config.isSet("lore")) setLore(config.getString("lore"));
        if (config.isSet("max-stack-size")) setMaxStackSize(config.getInt("max-stack-size"));
        if (config.isSet("item-level")) setItemLevel(config.getInt("item-level"));
        if (config.isSet("price")) setSellPrice(RaidCraft.getEconomy().parseCurrencyInput(config.getString("price")));
        if (config.isSet("bind-type")) setBindType(ItemBindType.fromString(config.getString("bind-type")));
        if (config.isSet("quality")) setQuality(ItemQuality.fromString(config.getString("quality")));
        buildTooltips();
    }
}
