package org.example.cart.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.example.inventory.item.domain.Item;
import org.example.user.domains.User;


@Entity
@Table(name = "cart")
public class ShoppingCart extends PanacheEntity {

    public int quantity;

    @ManyToOne
    public User user;

    @ManyToOne
    public Item shopItem;
}






