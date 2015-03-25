package de.raidcraft.items.useable;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.api.items.attachments.ConfiguredAttachment;
import de.raidcraft.api.items.attachments.ItemAttachment;
import de.raidcraft.api.items.attachments.ItemAttachmentException;
import de.raidcraft.api.items.attachments.ItemAttachmentManager;
import de.raidcraft.api.items.attachments.UseableCustomItem;
import de.raidcraft.api.items.attachments.UseableItemAttachment;
import de.raidcraft.items.items.DatabaseItem;
import de.raidcraft.items.tables.items.TCustomItem;
import org.bukkit.entity.Player;

/**
 * @author Silthus
 */
public class UseableItem extends DatabaseItem implements UseableCustomItem {

    public UseableItem(TCustomItem item) {

        super(item, ItemType.USEABLE);
    }

    @Override
    public void use(Player player, CustomItemStack itemStack) throws ItemAttachmentException {

        for (String attachmentName : attachments.keySet()) {
            ConfiguredAttachment section = attachments.get(attachmentName);
            ItemAttachment attachment = RaidCraft.getComponent(ItemAttachmentManager.class)
                    .getItemAttachment(section.getProvider(), attachmentName, player);
            if (attachment instanceof UseableItemAttachment) {
                ((UseableItemAttachment) attachment).use(itemStack, player, section);
            }
        }
    }
}
