package de.raidcraft.items.tables.items;

import de.raidcraft.api.ebean.BaseModel;
import de.raidcraft.api.items.attachments.Consumeable;
import io.ebean.annotation.NotNull;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "rcitems_consumeables")
public class TConsumeableItem extends BaseModel {

    @OneToOne(cascade = CascadeType.REMOVE)
    @NotNull
    @Column(unique = true)
    private TCustomItem item;

    @NotNull
    private Consumeable.Type type = Consumeable.Type.HEALTH;

    private String resourceName = null;
    private double resourceGain = 0;
    private boolean percentage = false;
    private boolean instant = false;
    private String intervall = null;
    private String duration = null;
}
