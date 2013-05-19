/*
 *  This file is part of RPG Items.
 *
 *  RPG Items is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  RPG Items is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with RPG Items.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.raidcraft.items.util;

import de.raidcraft.RaidCraft;
import de.raidcraft.items.ItemsPlugin;

import java.io.IOException;
import java.io.InputStream;

public class Font {

    public final static int[] WIDTHS;

    static  {
        WIDTHS = new int[0xFFFF];

        try {
            InputStream in = RaidCraft.getComponent(ItemsPlugin.class).getResource("defaults/font.bin");
            for (int i = 0; i < WIDTHS.length; i++) {
                WIDTHS[i] = in.read();
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
