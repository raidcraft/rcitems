package de.raidcraft.items.items;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.ItemCategory;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.items.tables.items.TCustomItem;
import org.bukkit.Material;

/**
 * @author Silthus
 */
public abstract class DatabaseItem extends AbstractCustomItem {

    public DatabaseItem(TCustomItem item, ItemType type) {

        super(item.getId(), item.getName(), type);
        Material material = Material.matchMaterial(item.getMinecraftItem());
        if (material == null) {
            RaidCraft.LOGGER.warning("INVALID minecraft material " + item.getMinecraftItem() + " in custom item " + getName() + "(ID: " + getId() + ")");
        }
        setMinecraftItem(material);
        setMinecraftDataValue((short) item.getMinecraftDataValue());
        setBindType(item.getBindType());
        setSellPrice(item.getSellPrice());
        setBlockingUsage(item.isBlockUsage());
        setLootable(item.isLootable());
        setMaxStackSize(item.getMaxStackSize());
        setLore(item.getLore());
        setItemLevel(item.getItemLevel());
        setQuality(item.getQuality());
        setEnchantmentEffect(item.isEnchantmentEffect());
        item.getCategories().forEach(category -> {
            ItemCategory itemCategory = new ItemCategory(category.getName());
            itemCategory.setDescription(category.getDescription());
            addCategory(itemCategory);
        });
        buildTooltips();
    }
}
