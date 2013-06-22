package de.raidcraft.items.crafting;

import com.avaje.ebean.annotation.EnumValue;
import de.raidcraft.util.EnumUtils;

/**
 * @author Silthus
 */
public enum CraftingRecipeType {

    @EnumValue("SHAPED")
    SHAPED,
    @EnumValue("SHAPELESS")
    SHAPELESS,
    @EnumValue("FURNACE")
    FURNACE,
    @EnumValue("ANVIL")
    ANVIL;

    public static CraftingRecipeType fromString(String str) {

        return EnumUtils.getEnumFromString(CraftingRecipeType.class, str);
    }
}
