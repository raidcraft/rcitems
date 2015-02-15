package de.raidcraft.items;

import de.raidcraft.api.items.ItemType;
import de.raidcraft.items.tables.items.TCustomItem;
import org.bukkit.Material;

/**
 * @author Silthus
 */
public abstract class DatabaseItem extends AbstractCustomItem {

    public DatabaseItem(TCustomItem item, ItemType type) {

        super(item.getId(), item.getName(), type);
        setBindType(item.getBindType());
        setSellPrice(item.getSellPrice());
        setMaxStackSize(item.getMaxStackSize());
        setMinecraftId(item.getMinecraftId());
        setMinecraftItem(Material.getMaterial(item.getMinecraftItem()));
        setMinecraftDataValue((short) item.getMinecraftDataValue());
        setLore(item.getLore());
        setItemLevel(item.getItemLevel());
        setQuality(item.getQuality());
        buildTooltips();
    }
}