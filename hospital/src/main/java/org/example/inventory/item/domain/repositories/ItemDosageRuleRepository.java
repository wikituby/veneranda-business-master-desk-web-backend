package org.example.inventory.item.domain.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.inventory.item.domain.ItemDosageRule;

@ApplicationScoped
public class ItemDosageRuleRepository implements PanacheRepository<ItemDosageRule> {
}
