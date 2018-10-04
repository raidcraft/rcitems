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
        String[] strings = item.getMinecraftItem().split("\\:");
        if (strings.length > 1) {
            try {
                if (strings[0].equalsIgnoreCase("minecraft") && strings.length > 2) {
                    setMinecraftDataValue(Short.parseShort(strings[2]));
                } else {
                    setMinecraftDataValue(Short.parseShort(strings[1]));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            setMinecraftDataValue((short) item.getMinecraftDataValue());
        }
        Material material;
        if (item.getMinecraftItem().endsWith(":" + item.getMinecraftDataValue())) {
            material = Material.matchMaterial(item.getMinecraftItem().replace(":" + item.getMinecraftDataValue(), ""));
        } else {
            material = Material.matchMaterial(item.getMinecraftItem());
        }
        if (material == null) {
            RaidCraft.LOGGER.warning("INVALID minecraft material " + item.getMinecraftItem() + " in custom item " + getName() + "(ID: " + getId() + ")");
        }
        setMinecraftItem(material);
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
