package de.raidcraft.items;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.items.tables.TCustomItem;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
public abstract class BaseItem implements CustomItem {

    private final int id;
    private final int minecraftId;
    private final short minecraftData;
    private final String name;
    private final int itemLevel;
    private final ItemQuality quality;
    private final double sellPrice;

    public BaseItem(TCustomItem item) {

        this.id = item.getId();
        this.minecraftId = item.getMinecraftId();
        this.minecraftData = (short) item.getMinecraftDataValue();
        this.name = item.getName();
        this.itemLevel = item.getItemLevel();
        this.quality = item.getQuality();
        this.sellPrice = item.getSellPrice();
    }

    @Override
    public int getId() {

        return id;
    }

    @Override
    public int getMinecraftId() {

        return minecraftId;
    }

    @Override
    public short getMinecraftDataValue() {

        return minecraftData;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public int getItemLevel() {

        return itemLevel;
    }

    @Override
    public ItemQuality getQuality() {

        return quality;
    }

    @Override
    public double getSellPrice() {

        return sellPrice;
    }

    @Override
    public final boolean matches(ItemStack itemStack) {

        return ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()).equals(getName());
    }

    protected abstract ItemMeta modifiyItemMeta(ItemMeta item);

    private ItemMeta getHeader(ItemMeta item) {

        item.setDisplayName(getQuality().getColor() + getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Gegenstandsstufe " + getItemLevel());
        item.setLore(lore);
        return item;
    }

    @Override
    public final CustomItemStack createNewItem() {

        ItemStack itemStack = new ItemStack(getMinecraftId(), 1, getMinecraftDataValue());
        ItemMeta itemMeta = modifiyItemMeta(getHeader(itemStack.getItemMeta()));
        if (getSellPrice() > 0) {
            // always add the sell price last
            itemMeta.getLore().add("Verkaufspreis: " + getSellPrice());
        }
        itemStack.setItemMeta(itemMeta);
        return RaidCraft.getCustomItem(itemStack);
    }
}
