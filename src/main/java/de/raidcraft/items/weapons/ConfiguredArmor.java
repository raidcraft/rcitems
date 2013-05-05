package de.raidcraft.items.weapons;

import de.raidcraft.api.items.ArmorType;
import de.raidcraft.api.items.Attribute;
import de.raidcraft.api.items.CustomArmor;
import de.raidcraft.items.BaseEquipment;
import de.raidcraft.items.BaseItem;
import de.raidcraft.items.tables.TCustomArmor;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
public class ConfiguredArmor extends BaseEquipment implements CustomArmor {

    private final ArmorType armorType;
    private final int armorValue;

    public ConfiguredArmor(TCustomArmor armor) {

        super(armor.getEquipment());
        this.armorType = armor.getArmorType();
        this.armorValue = armor.getArmorValue();
    }

    public ArmorType getArmorType() {

        return armorType;
    }

    public int getArmorValue() {

        return armorValue;
    }

    @Override
    protected List<String> getCustomTooltipLines() {

        ArrayList<String> output = new ArrayList<>();

        output.add(getArmorType().getEquipmentSlot(getMinecraftId()).getGermanName() + BaseItem.LINE_SEPARATOR + getArmorType().getGermanName());
        output.add(getArmorValue() + " Rüstung");

        for (Attribute attribute : getSortedAttributes()) {
            String str = "+";
            if (attribute.getValue() < 0) {
                str = ChatColor.RED + "-";
            }
            str += attribute.getValue() + " " + attribute.getName();
            output.add(str);
        }
        return output;
    }
}
