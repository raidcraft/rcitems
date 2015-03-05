package de.raidcraft.items;

import de.raidcraft.api.items.ItemType;
import org.bukkit.Material;

/**
 * @author Silthus
 */
public class DynamicCustomItem extends AbstractCustomItem {

    public static Builder create(String name) {

        return new Builder(new DynamicCustomItem(0, name, ItemType.UNDEFINED));
    }

    private DynamicCustomItem(int id, String name, ItemType type) {

        super(id, name, type);
    }

    public static class Builder {

        private final DynamicCustomItem item;

        private Builder(DynamicCustomItem item) {

            this.item = item;
        }

        public Builder type(ItemType type) {

            item.setType(type);
            return this;
        }

        public Builder item(Material item) {

            this.item.setMinecraftItem(item);
            return this;
        }

        public Builder data(short data) {

            if (data < 1) return this;
            this.item.setMinecraftDataValue(data);
            return this;
        }

        public Builder lore(String lore) {

            this.item.setLore(lore);
            return this;
        }

        public Builder stackSize(int maxStackSize) {

            this.item.setMaxStackSize(maxStackSize);
            return this;
        }

        public Builder level(int level) {

            this.item.setItemLevel(level);
            return this;
        }

        public Builder sellPrice(double sellPrice) {

            this.item.setSellPrice(sellPrice);
            return this;
        }
    }
}
