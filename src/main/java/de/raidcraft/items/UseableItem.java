package de.raidcraft.items;

import de.raidcraft.api.items.Useable;
import de.raidcraft.items.tables.TCustomItem;

/**
 * @author Silthus
 */
public abstract class UseableItem<T> extends BaseItem implements Useable<T> {

    public UseableItem(TCustomItem item) {

        super(item);
    }
}
