package de.raidcraft.items.tables.items;

import com.avaje.ebean.validation.NotNull;
import de.raidcraft.api.items.ItemBindType;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.items.ItemType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_items")
@Getter
@Setter
public class TCustomItem {

    @Id
    private int id;
    @NotNull
    @Column(unique = true)
    private String name;
    private String lore;
    @NotNull
    private int minecraftId;
    private String minecraftItem;
    @NotNull
    private int minecraftDataValue;
    @NotNull
    private int itemLevel;
    @NotNull
    private ItemQuality quality;
    @NotNull
    private int maxStackSize;
    @NotNull
    private double sellPrice = 0.0;
    @NotNull
    private ItemBindType bindType = ItemBindType.NONE;
    @NotNull
    private ItemType itemType;
    private boolean blockUsage = false;
    private boolean lootable = true;
    private boolean enchantmentEffect = false;
    private String info;
    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "item_id")
    private List<TCustomItemAttachment> attachments;
}
