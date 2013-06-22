package de.raidcraft.items;

import de.raidcraft.items.tables.items.TCustomItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
public class SimpleItem extends BaseItem {

    public SimpleItem(TCustomItem item) {

        super(item);
    }

    @Override
    protected List<String> getCustomTooltipLines() {

        return new ArrayList<>();
    }
}
