package de.raidcraft.items.tables.items;

import de.raidcraft.api.items.WeaponType;
import io.ebean.annotation.NotNull;

import javax.persistence.*;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_weapons")
public class TCustomWeapon {

    @Id
    private int id;
    @OneToOne(cascade = CascadeType.REMOVE)
    @NotNull
    @Column(unique = true)
    private TCustomEquipment equipment;
    @NotNull
    private WeaponType weaponType;
    @NotNull
    private int minDamage;
    @NotNull
    private int maxDamage;
    @NotNull
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
