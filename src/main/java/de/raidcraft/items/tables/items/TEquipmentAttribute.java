package de.raidcraft.items.tables.items;

import de.raidcraft.api.items.AttributeType;
import io.ebean.annotation.NotNull;

import javax.persistence.*;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_equipment_attributes")
public class TEquipmentAttribute {

    @Id
    private int id;
    @NotNull
    @ManyToOne(cascade = CascadeType.REMOVE)
    private TCustomEquipment equipment;
    @NotNull
    @ManyToOne(cascade = CascadeType.REMOVE)
    private AttributeType attribute;
    @NotNull
    private int attributeValue;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public TCustomEquipment getEquipment() {

        return equipment;
    }

    public void setEquipment(TCustomEquipment equipment) {

        this.equipment = equipment;
    }

    public AttributeType getAttribute() {

        return attribute;
    }

    public void setAttribute(AttributeType attribute) {

        this.attribute = attribute;
    }

    public int getAttributeValue() {

        return attributeValue;
    }

    public void setAttributeValue(int attributeValue) {

        this.attributeValue = attributeValue;
    }
}
