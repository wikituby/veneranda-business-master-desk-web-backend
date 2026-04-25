package org.example.inventory.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Category extends PanacheEntity {

    @Column
    public String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    public Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    public List<Category> children;

}

