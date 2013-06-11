package de.raidcraft.items.equipment;

import de.raidcraft.api.items.CustomWeapon;
import de.raidcraft.api.items.ItemAttribute;
import de.raidcraft.api.items.WeaponType;
import de.raidcraft.items.BaseEquipment;
import de.raidcraft.items.BaseItem;
import de.raidcraft.items.tables.TCustomWeapon;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
public class ConfiguredWeapon extends BaseEquipment implements CustomWeapon {

    private final WeaponType type;
    private final int minDamage;
    private final int maxDamage;
    private final double swingTime;

    public ConfiguredWeapon(TCustomWeapon weapon) {

        super(weapon.getEquipment());
        this.type = weapon.getWeaponType();
        this.minDamage = weapon.getMinDamage();
        this.maxDamage = weapon.getMaxDamage();
        this.swingTime = weapon.getSwingTime();
    }

    @Override
    protected List<String> getCustomTooltipLines() {

        ArrayList<String> output = new ArrayList<>();
        output.add(getWeaponType().getEquipmentSlot().getGermanName() + BaseItem.LINE_SEPARATOR + getWeaponType().getGermanName());
        String damageStr;
        if (getMinDamage() == 0 && getMaxDamage() == 0) {
            damageStr = null;
        } else if (getMinDamage() == getMaxDamage()) {
            damageStr = getMaxDamage() + " Schaden";
        } else {
            damageStr = getMinDamage() + "-" + getMaxDamage() + " Schaden";
        }
        if (damageStr != null) {
            damageStr += BaseItem.LINE_SEPARATOR + "Tempo " + CustomItemUtil.getSwingTimeString(getSwingTime());
            output.add(damageStr);
            output.add("(" + getDamagePerSecond() + " Schaden pro Sekunde)");
        }
        for (ItemAttribute attribute : getSortedAttributes()) {
            String str = "+";
            if (attribute.getValue() < 0) {
                str = ChatColor.RED + "-";
            }
            str += attribute.getValue() + " " + attribute.getDisplayName();
            output.add(str);
        }
        return output;
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
}