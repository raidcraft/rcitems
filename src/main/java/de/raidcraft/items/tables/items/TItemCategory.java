package de.raidcraft.items.tables.items;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mdoering
 */
@Entity
@Getter
@Setter
@Table(name = "rcitems_categories")
public class TItemCategory {

    @Id
    private int id;
    private String name;
    private String description;
    @ManyToMany(mappedBy = "categories")
    private List<TCustomItem> items = new ArrayList<>();
}
