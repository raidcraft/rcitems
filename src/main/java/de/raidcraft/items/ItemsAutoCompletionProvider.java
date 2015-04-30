package de.raidcraft.items;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.chat.AutoCompletionProvider;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemManager;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.items.listener.PlayerListener;
import de.raidcraft.util.CustomItemUtil;
import mkremins.fanciful.FancyMessage;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author mdoering
 */
public class ItemsAutoCompletionProvider extends AutoCompletionProvider {

    public ItemsAutoCompletionProvider() {

        super('#', 3, "Wenn du Items mit #[Tab] vervollständigen willst, " +
                "dann klicke diese bitte zuerst mit der mittleren Maustaste an oder nutze mindestens 3 Buchstaben.");
    }

    @Override
    protected List<String> getAutoCompleteList(Player player, @Nullable String message) {

        List<CustomItemStack> autoList = PlayerListener.getAutoCompleteItems().get(player.getUniqueId());
        if (autoList == null) autoList = new ArrayList<>();
        if (!autoList.isEmpty()) {
            List<String> items = autoList.stream()
                    .filter(i -> message == null || i.getItem().getName().toLowerCase().startsWith(message))
                    .map(i -> i.getItem().getName())
                    .collect(Collectors.toList());
            if (!items.isEmpty()) {
                return items;
            }
        }
        if (message != null && message.length() > 2) {
            return RaidCraft.getComponent(CustomItemManager.class).getLoadedCustomItems().stream()
                    .filter(i -> i.getName().toLowerCase().startsWith(message))
                    .map(CustomItem::getName)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public FancyMessage autoComplete(Player player, FancyMessage fancyMessage, String item) {

        List<CustomItemStack> items = PlayerListener.getAutoCompleteItems().get(player.getUniqueId());
        if (items == null) items = new ArrayList<>();
        // lets first try to find a queued auto complete item
        Optional<CustomItemStack> first = items.stream()
                .filter(i -> i.getItem().getName().equals(item))
                .findFirst();
        if (first.isPresent()) {
            fancyMessage = CustomItemUtil.getFormattedItemTooltip(fancyMessage, first.get());
        } else {
            // if none is found ask our item cache
            Optional<CustomItem> match = RaidCraft.getComponent(CustomItemManager.class).getLoadedCustomItems().stream()
                    .filter(i -> i.getName().equals(item))
                    .findFirst();
            if (match.isPresent()) {
                fancyMessage = CustomItemUtil.getFormattedItemTooltip(fancyMessage, match.get());
            }
        }
        return fancyMessage;
    }
}
