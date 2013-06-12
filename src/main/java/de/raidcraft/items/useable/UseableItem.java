package de.raidcraft.items.useable;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.attachments.ItemAttachment;
import de.raidcraft.api.items.attachments.ItemAttachmentException;
import de.raidcraft.api.items.attachments.ItemAttachmentManager;
import de.raidcraft.api.items.attachments.UseableCustomItem;
import de.raidcraft.api.items.attachments.UseableItemAttachment;
import de.raidcraft.items.BaseItem;
import de.raidcraft.items.tables.TCustomItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
public class UseableItem extends BaseItem implements UseableCustomItem {

    public UseableItem(TCustomItem item) {

        super(item);
    }

    @Override
    protected List<String> getCustomTooltipLines() {

        return new ArrayList<>();
    }

    @Override
    public void use(Player player) throws ItemAttachmentException {

        for (String attachmentName : attachments.keySet()) {
            ConfigurationSection section = attachments.get(attachmentName);
            ItemAttachment attachment = RaidCraft.getComponent(ItemAttachmentManager.class)
                    .getItemAttachment(section.getString("provider"), attachmentName, player);
            if (attachment instanceof UseableItemAttachment) {
                ((UseableItemAttachment) attachment).use(this, player, section);
            }
        }
    }
}
