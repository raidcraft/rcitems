package de.raidcraft.items.configs;

import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.util.StringUtils;

import java.io.File;

/**
 * @author Silthus
 */
public class AttachmentConfig extends ConfigurationBase<ItemsPlugin> {

    private final String name;

    public AttachmentConfig(ItemsPlugin plugin, File file) {

        super(plugin, file);
        this.name = StringUtils.formatName(file.getName()).replace(".yml", "");
    }

    @Override
    public String getName() {

        return name;
    }
}
