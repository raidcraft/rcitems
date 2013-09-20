package de.raidcraft.items.equipment;

import de.raidcraft.api.items.ArmorType;
import de.raidcraft.api.items.CustomArmor;
import de.raidcraft.api.items.SingleLineTooltip;
import de.raidcraft.api.items.Tooltip;
import de.raidcraft.api.items.TooltipSlot;
import de.raidcraft.items.BaseEquipment;
import de.raidcraft.items.tables.items.TCustomArmor;

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
        setTooltip(new SingleLineTooltip(
                TooltipSlot.EQUIPMENT_TYPE, getEquipmentSlot().getGermanName() + Tooltip.LINE_SEPARATOR + getArmorType().getGermanName()));
        setTooltip(new SingleLineTooltip(TooltipSlot.ARMOR, getArmorValue() + " Rüstung"));
    }

    public ArmorType getArmorType() {

        return armorType;
    }

    public int getArmorValue() {

        return armorValue;
    }
}
