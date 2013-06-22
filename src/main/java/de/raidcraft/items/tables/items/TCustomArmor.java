package de.raidcraft.items.tables.items;

import com.avaje.ebean.validation.NotNull;
import de.raidcraft.api.items.ArmorType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_armor")
public class TCustomArmor {

    @Id
    private int id;
    @OneToOne
    @Column(unique = true)
    private TCustomEquipment equipment;
    @NotNull
    private ArmorType armorType;
    @NotNull
    private int armorValue;

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

    public ArmorType getArmorType() {

        return armorType;
    }

    public void setArmorType(ArmorType armorType) {

        this.armorType = armorType;
    }

    public int getArmorValue() {

        return armorValue;
    }

    public void setArmorValue(int armorValue) {

        this.armorValue = armorValue;
    }
}
