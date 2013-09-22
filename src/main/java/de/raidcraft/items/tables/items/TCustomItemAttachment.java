package de.raidcraft.items.tables.items;

import com.avaje.ebean.validation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

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
    private String color;
    @OneToMany
    @JoinColumn(name = "attachment_id")
    private List<TItemAttachmentData> itemAttachmentDataList;

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

    public List<TItemAttachmentData> getItemAttachmentDataList() {

        return itemAttachmentDataList;
    }

    public void setItemAttachmentDataList(List<TItemAttachmentData> itemAttachmentDataList) {

        this.itemAttachmentDataList = itemAttachmentDataList;
    }

    public String getColor() {

        return color;
    }

    public void setColor(String color) {

        this.color = color;
    }
}
