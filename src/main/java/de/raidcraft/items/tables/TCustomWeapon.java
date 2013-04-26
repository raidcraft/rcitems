package de.raidcraft.items.tables;

import com.avaje.ebean.validation.NotNull;
import de.raidcraft.api.items.WeaponType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_weapons")
public class TCustomWeapon {

    @Id
    private int id;
    @OneToOne
    @Column(unique = true)
    private TCustomEquipment equipment;
    @NotNull
    private WeaponType weaponType;
    private int minDamage;
    private int maxDamage;
    private double swingTime;

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

    public WeaponType getWeaponType() {

        return weaponType;
    }

    public void setWeaponType(WeaponType weaponType) {

        this.weaponType = weaponType;
    }

    public int getMinDamage() {

        return minDamage;
    }

    public void setMinDamage(int minDamage) {

        this.minDamage = minDamage;
    }

    public int getMaxDamage() {

        return maxDamage;
    }

    public void setMaxDamage(int maxDamage) {

        this.maxDamage = maxDamage;
    }

    public double getSwingTime() {

        return swingTime;
    }

    public void setSwingTime(double swingTime) {

        this.swingTime = swingTime;
    }
}
