package de.raidcraft.items.tables;

import com.avaje.ebean.validation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_attachments")
public class TCustomItemAttachment {

    @Id
    private int id;
    @NotNull
    @ManyToOne
    private TCustomItem item;
    @NotNull
    private String attachmentName;
    @NotNull
    private String providerName;
    private String description;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public TCustomItem getItem() {

        return item;
    }

    public void setItem(TCustomItem item) {

        this.item = item;
    }

    public String getAttachmentName() {

        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {

        this.attachmentName = attachmentName;
    }

    public String getProviderName() {

        return providerName;
    }

    public void setProviderName(String providerName) {

        this.providerName = providerName;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }
}
