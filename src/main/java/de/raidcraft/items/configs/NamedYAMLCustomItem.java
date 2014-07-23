package de.raidcraft.items.configs;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItem;
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

    public NamedYAMLCustomItem(String name, ConfigurationSection config) {

        super(CustomItem.NAMED_CUSTOM_ITEM_ID, name, ItemType.fromString(config.getString("type", "Undefined")));
        setMinecraftItem(Material.getMaterial(config.getString("item")));
        short dataValue = (short) config.getInt("item-data", 0);
        if (dataValue > 0) setMinecraftDataValue(dataValue);
        setLore(config.getString("lore"));
        setMaxStackSize(config.getInt("max-stack-size", 1));
        setItemLevel(config.getInt("item-level"));
        setSellPrice(RaidCraft.getEconomy().parseCurrencyInput(config.getString("price")));
        setBindType(ItemBindType.valueOf(config.getString("bind-type", "NONE")));
        setQuality(ItemQuality.fromString(config.getString("quality", "COMMON")));
        buildTooltips();
    }
}
