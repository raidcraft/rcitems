package de.raidcraft.items.trigger;

import com.comphenix.packetwrapper.PacketWrapper;
import com.comphenix.packetwrapper.WrapperPlayServerSetSlot;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.trigger.Trigger;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.util.ConfigUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class InventoryTrigger extends Trigger {

    private static final short PLAYER_INVENTORY_ID = 0;

    private final ItemsPlugin plugin;

    public InventoryTrigger(ItemsPlugin plugin) {
        super("inventory", "update");
        this.plugin = plugin;
        registerPacketListener();
    }

    @Information(
            value = "inventory.update",
            desc = "Triggers if an inventory update packet is sent from the server to the client.",
            conf = {
                    "item: checks if the given item was set into a slot (optional)"
            }
    )
    public void registerPacketListener() {

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketReceiving(PacketEvent event) {

                WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(event.getPacket());
                if (packet.getWindowId() != PLAYER_INVENTORY_ID) return;
                if (packet.getSlotData() == null || packet.getSlotData().getType() == Material.AIR) return;

                informListeners("update", event.getPlayer(), config -> {
                    if (!config.isSet("item")) return true;
                    Optional<ItemStack> item = RaidCraft.getItem(config.getString("item"));
                    if (!item.isPresent()) {
                        plugin.getLogger().warning("Invalid item " + config.getString("item") + " in " + ConfigUtil.getFileName(config));
                    }
                    return item.map(itemStack -> itemStack.isSimilar(packet.getSlotData()))
                            .orElse(false);
                });
            }
        });
    }
}
