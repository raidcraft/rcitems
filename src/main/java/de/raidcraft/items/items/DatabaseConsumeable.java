package de.raidcraft.items.items;

import de.raidcraft.api.items.ItemType;
import de.raidcraft.api.items.attachments.Consumeable;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.api.items.tooltip.VariableMultilineTooltip;
import de.raidcraft.items.tables.items.TConsumeableItem;
import de.raidcraft.util.TimeUtil;
import lombok.Data;
import org.bukkit.ChatColor;

@Data
public class DatabaseConsumeable extends DatabaseItem implements Consumeable {

    private final Type consumeableType;
    private final String resourceName;
    private final double duration;
    private final long interval;
    private final double resourceGain;
    private final boolean percentage;

    public DatabaseConsumeable(TConsumeableItem item, ItemType type) {
        super(item.getItem(), type);
        this.consumeableType = item.getType();
        this.resourceName = item.getResourceName();
        this.duration = TimeUtil.parseTimeAsSeconds(item.getDuration());
        this.interval = TimeUtil.parseTimeAsTicks(item.getIntervall());
        this.resourceGain = item.getResourceGain();
        this.percentage = item.isPercentage();
    }

    @Override
    protected void buildTooltips() {
        super.buildTooltips();
        setTooltip(new VariableMultilineTooltip(TooltipSlot.CONSUMEABLE, "Regeneriert " + getResourceName(), false, false, ChatColor.GREEN));
    }
}
