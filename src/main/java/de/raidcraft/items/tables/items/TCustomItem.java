package de.raidcraft.items.tables.items;

import de.raidcraft.api.items.ItemBindType;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.items.ItemType;
import io.ebean.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
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
    private String minecraftItem;
    @NotNull
    private int minecraftDataValue;
    @NotNull
    private int itemLevel;
    @NotNull
    private ItemQuality quality = ItemQuality.COMMON;
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
    private List<TCustomItemAttachment> attachments = new ArrayList<>();
    @ManyToMany
    @JoinTable(name = "rcitems_item_categories",
            joinColumns=@JoinColumn(name="item_id", referencedColumnName="id"),
            inverseJoinColumns=@JoinColumn(name="category_id", referencedColumnName="id")
    )
    private List<TItemCategory> categories = new ArrayList<>();
}
