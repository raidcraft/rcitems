package de.raidcraft.items.equipment;

import de.raidcraft.api.items.CustomWeapon;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.api.items.WeaponType;
import de.raidcraft.api.items.tooltip.DPSTooltip;
import de.raidcraft.api.items.tooltip.EquipmentTypeTooltip;
import de.raidcraft.api.items.tooltip.SingleLineTooltip;
import de.raidcraft.api.items.tooltip.Tooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.items.items.DatabaseEquipment;
import de.raidcraft.items.tables.items.TCustomWeapon;
import de.raidcraft.util.CustomItemUtil;

/**
 * @author Silthus
 */
public class ConfiguredWeapon extends DatabaseEquipment implements CustomWeapon {

    private final WeaponType type;
    private final int minDamage;
    private final int maxDamage;
    private final double swingTime;

    public ConfiguredWeapon(TCustomWeapon weapon) {

        super(weapon.getEquipment(), ItemType.WEAPON);
        this.type = weapon.getWeaponType();
        this.minDamage = weapon.getMinDamage();
        this.maxDamage = weapon.getMaxDamage();
        this.swingTime = weapon.getSwingTime();
        // set our tooltip lines
        setTooltip(new EquipmentTypeTooltip(getEquipmentSlot().getGermanName() + Tooltip.LINE_SEPARATOR + getWeaponType().getGermanName()));
        // damage tooltip
        String damageStr;
        if (getMinDamage() == 0 && getMaxDamage() == 0) {
            damageStr = null;
        } else if (getMinDamage() == getMaxDamage()) {
            damageStr = getMaxDamage() + " Schaden";
        } else {
            damageStr = getMinDamage() + "-" + getMaxDamage() + " Schaden";
        }
        if (damageStr != null) {
            damageStr += Tooltip.LINE_SEPARATOR + "Tempo " + CustomItemUtil.getSwingTimeString(getSwingTime());
            setTooltip(new SingleLineTooltip(TooltipSlot.DAMAGE, damageStr));
            setTooltip(new DPSTooltip(getDamagePerSecond()));
        }
    }

    @Override
    public WeaponType getWeaponType() {

        return type;
    }

    @Override
    public int getMinDamage() {

        return minDamage;
    }

    @Override
    public int getMaxDamage() {

        return maxDamage;
    }

    @Override
    public double getSwingTime() {

        return swingTime;
    }

    protected double getDamagePerSecond() {

        int averageDamage = (getMinDamage() + getMaxDamage()) / 2;
        double dps = averageDamage / getSwingTime();
        return ((int) (dps * 10) / 10.0);
    }

    @Override
    public String toString() {

        return "ConfiguredWeapon{" +
                "type=" + type +
                ", minDamage=" + minDamage +
                ", maxDamage=" + maxDamage +
                ", swingTime=" + swingTime +
                "} " + super.toString();
    }
}
