package de.raidcraft.items.tables;

import com.avaje.ebean.validation.NotNull;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_attributes")
public class TAttribute {

    @Id
    private int id;
    @NotNull
    @Column(unique = true)
    private String name;
    private String displayName;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "attribute_id")
    private List<TEquipmentAttribute> attributes;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public List<TEquipmentAttribute> getAttributes() {

        return attributes;
    }

    public void setAttributes(List<TEquipmentAttribute> attributes) {

        this.attributes = attributes;
    }
}
