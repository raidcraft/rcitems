package de.raidcraft.items.tables.items;

import com.avaje.ebean.validation.NotNull;
import de.raidcraft.api.items.AttributeType;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_equipment_attributes")
public class TEquipmentAttribute {

    @Id
    private int id;
    @NotNull
    @ManyToOne
    private TCustomEquipment equipment;
    @NotNull
    @ManyToOne
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
