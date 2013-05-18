package de.raidcraft.items.tables;

import com.avaje.ebean.validation.NotNull;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.items.ItemType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_items")
public class TCustomItem {

    @Id
    private int id;
    @NotNull
    @Column(unique = true)
    private String name;
    private String lore;
    @NotNull
    private int minecraftId;
    @NotNull
    private int minecraftDataValue;
    @NotNull
    private int itemLevel;
    @NotNull
    private ItemQuality quality;
    private double sellPrice;
    @NotNull
    private ItemType itemType;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getLore() {

        return lore;
    }

    public void setLore(String lore) {

        this.lore = lore;
    }

    public int getMinecraftId() {

        return minecraftId;
    }

    public void setMinecraftId(int minecraftId) {

        this.minecraftId = minecraftId;
    }

    public int getMinecraftDataValue() {

        return minecraftDataValue;
    }

    public void setMinecraftDataValue(int minecraftDataValue) {

        this.minecraftDataValue = minecraftDataValue;
    }

    public int getItemLevel() {

        return itemLevel;
    }

    public void setItemLevel(int itemLevel) {

        this.itemLevel = itemLevel;
    }

    public ItemQuality getQuality() {

        return quality;
    }

    public void setQuality(ItemQuality quality) {

        this.quality = quality;
    }

    public double getSellPrice() {

        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {

        this.sellPrice = sellPrice;
    }

    public ItemType getItemType() {

        return itemType;
    }

    public void setItemType(ItemType itemType) {

        this.itemType = itemType;
    }
}
