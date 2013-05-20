package de.raidcraft.items.util;

import de.raidcraft.api.items.CustomItemException;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Silthus
 */
public final class CustomItemUtil {

    public static String encodeItemId(int id) {

        String hex = String.format("%08x", id);
        StringBuilder out = new StringBuilder();
        for (char h : hex.toCharArray()) {
            out.append(ChatColor.COLOR_CHAR);
            out.append(h);
        }
        return out.toString();
    }

    public static boolean isCustomItem(ItemStack itemStack) {

        if (itemStack == null) return false;
        try {
            decodeItemId(itemStack.getItemMeta());
            return true;
        } catch (CustomItemException ignored) {
        }
        return false;
    }

    public static int decodeItemId(ItemMeta itemMeta) throws CustomItemException {

        if (itemMeta == null || !itemMeta.hasDisplayName() || !itemMeta.hasLore()) {
            throw new CustomItemException("Item ist kein Custom Item.");
        }
        return decodeItemId(itemMeta.getDisplayName());
    }

    public static int decodeItemId(String str) throws CustomItemException {

        if (str.length() < 16) {
            throw new CustomItemException("Item ist kein Custom Item.");
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            if (str.charAt(i) != ChatColor.COLOR_CHAR)
                throw new CustomItemException("Item ist kein Custom Item.");
            i++;
            out.append(str.charAt(i));
        }
        return Integer.parseInt(out.toString(), 16);
    }

    public static int getStringWidth(String str) {

        str = ChatColor.stripColor(str);
        int width = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            width += Font.WIDTHS[c] + 1;
        }
        return width;
    }

    public static int getStringWidthBold(String str) {

        str = ChatColor.stripColor(str);
        int width = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            width += Font.WIDTHS[c] + 2;
        }
        return width;
    }

    public static int checkWidth(String str, int width) {

        return checkWidth(str, width, false);
    }

    public static int checkWidth(String str, int width, boolean bold) {

        if (bold) {
            int dWidth = getStringWidthBold(str);
            if (dWidth > width) return dWidth;
            return width;
        } else {
            int dWidth = getStringWidth(str);
            if (dWidth > width) return dWidth;
            return width;
        }
    }

    public static String getSellPriceString(double price) {

        if (price > 0.0) {
            String[] split = Double.toString(price).split("\\.");
            if (split.length < 2) {
                return ChatColor.WHITE + split[0] + ChatColor.GOLD + "●";
            }
            return ChatColor.WHITE + split[0] + ChatColor.GOLD + "● " + ChatColor.WHITE + split[1] + ChatColor.GRAY + "●";
        }
        return null;
    }

    public static String getSwingTimeString(double time) {

        time = (int)(time * 100) / 100.0;
        return Double.toString(time);
    }
}
