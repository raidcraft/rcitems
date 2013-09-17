package de.raidcraft.items.tables.items;

import de.raidcraft.api.config.KeyValueMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_attachment_data")
public class TItemAttachmentData implements KeyValueMap {

    private int id;
    @ManyToOne
    @Column(name = "attachment_id")
    private TCustomItemAttachment attachment;
    private String dataKey;
    private String dataValue;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public TCustomItemAttachment getAttachment() {

        return attachment;
    }

    public void setAttachment(TCustomItemAttachment attachment) {

        this.attachment = attachment;
    }

    public String getDataKey() {

        return dataKey;
    }

    public void setDataKey(String dataKey) {

        this.dataKey = dataKey;
    }

    public String getDataValue() {

        return dataValue;
    }

    public void setDataValue(String dataValue) {

        this.dataValue = dataValue;
    }
}
