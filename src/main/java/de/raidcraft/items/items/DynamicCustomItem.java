package de.raidcraft.items.items;

import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.ItemBindType;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.api.items.tooltip.Tooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.api.items.tooltip.VariableMultilineTooltip;
import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * @author Silthus
 */
public class DynamicCustomItem extends AbstractCustomItem {

    private Tooltip miscTooltip;

    public static Builder create(String name) {

        return new Builder(new DynamicCustomItem(CustomItem.DYNAMIC_CUSTOM_ITEM_ID, name, ItemType.UNDEFINED));
    }

    private DynamicCustomItem(int id, String name, ItemType type) {

        super(id, name, type);
    }

    public void setMiscTooltip(String original, boolean quote, boolean italic, ChatColor color) {

        this.miscTooltip = new VariableMultilineTooltip(TooltipSlot.MISC, original, quote, italic, color);
    }

    @Override
    protected void buildTooltips() {

        super.buildTooltips();
        if (miscTooltip != null) setTooltip(miscTooltip);
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

        public Builder bindType(ItemBindType bindType) {

            this.item.setBindType(bindType);
            return this;
        }

        public Builder quality(ItemQuality quality) {

            this.item.setQuality(quality);
            return this;
        }

        public Builder tooltip(String original, boolean quote, boolean italic, ChatColor color) {

            this.item.setMiscTooltip(original, quote, italic, color);
            return this;
        }

        public CustomItem build() {

            this.item.buildTooltips();
            return item;
        }
    }
}
