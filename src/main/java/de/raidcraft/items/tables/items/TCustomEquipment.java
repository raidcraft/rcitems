package de.raidcraft.items.tables.items;

import de.raidcraft.api.items.EquipmentSlot;
import de.raidcraft.api.items.ItemAttribute;
import io.ebean.annotation.NotNull;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_equipment")
@Data
public class TCustomEquipment {

    @Id
    private int id;
    @OneToOne(cascade = CascadeType.REMOVE)
    @NotNull
    @Column(unique = true)
    private TCustomItem item;
    @NotNull
    private EquipmentSlot equipmentSlot;
    @NotNull
    private int durability;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "equipment_id")
    private List<TEquipmentAttribute> attributes = new ArrayList<>();

    public Set<ItemAttribute> createAttributes() {

        Set<ItemAttribute> attributes = new HashSet<>();
        for (TEquipmentAttribute attribute : getAttributes()) {
            attributes.add(
                    new ItemAttribute(
                            attribute.getAttribute(),
                            attribute.getAttributeValue()
                    )
            );
        }
        return attributes;
    }
}
