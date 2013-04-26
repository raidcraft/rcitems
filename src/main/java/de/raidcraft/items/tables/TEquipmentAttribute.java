package de.raidcraft.items.tables;

import com.avaje.ebean.validation.NotNull;

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
    @ManyToOne
    private TCustomEquipment equipment;
    @NotNull
    private String attributeName;
    @NotNull
    private int attributeValue;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getAttributeName() {

        return attributeName;
    }

    public void setAttributeName(String attributeName) {

        this.attributeName = attributeName;
    }

    public int getAttributeValue() {

        return attributeValue;
    }

    public void setAttributeValue(int attributeValue) {

        this.attributeValue = attributeValue;
    }

    public TCustomEquipment getEquipment() {

        return equipment;
    }

    public void setEquipment(TCustomEquipment equipment) {

        this.equipment = equipment;
    }
}
