package de.raidcraft.items.weapons;

import de.raidcraft.api.items.Attribute;
import de.raidcraft.api.items.CustomWeapon;
import de.raidcraft.api.items.WeaponType;
import de.raidcraft.items.BaseEquipment;
import de.raidcraft.items.tables.TCustomWeapon;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;

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
    protected ItemMeta modifiyItemMeta(ItemMeta item) {

        List<String> lore = item.getLore();
        lore.add(ChatColor.WHITE + getWeaponType().getEquipmentSlot().getGermanName() + "          " + getWeaponType().getGermanName());
        lore.add(ChatColor.WHITE + "" + getMinDamage() + " - " + getMaxDamage() + " Schaden         " + "Tempo " + getSwingTime());
        lore.add(ChatColor.WHITE + "(" + getDamagePerSecond() + " Schaden pro Sekunde)");
        for (Attribute attribute : getAttributes()) {
            String str = ChatColor.WHITE + "+";
            if (attribute.getValue() < 0) {
                str = ChatColor.RED + "-";
            }
            str += attribute.getValue() + " " + attribute.getName();
            lore.add(str);
        }
        lore.add(ChatColor.WHITE + "");
        lore.add(ChatColor.WHITE + "Haltbarkeit " + getDurability() + "/" + getDurability());
        item.setLore(lore);
        return item;
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
