package org.example.cafeteria.client.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.cafeteria.client.domains.BuyerGroup;

@ApplicationScoped
public class BuyerGroupRepository implements PanacheRepository<BuyerGroup> {
    public BuyerGroup findByGroupNameAndShortName(String groupName, String groupNameShortForm) {
        // Normalize input
        String normalizedA = normalize(groupName);
        String normalizedB = normalize(groupNameShortForm);

        // If normalized values are equal, match any group where both fields equal that value (symmetry)
        if (normalizedA.equals(normalizedB)) {
            return find(
                    "lower(replace(groupName, ' ', '')) = ?1 and lower(replace(groupNameShortForm, ' ', '')) = ?1",
                    normalizedA
            ).firstResult();
        }

        // Else, match both combinations
        return find(
                "(lower(replace(groupName, ' ', '')) = ?1 and lower(replace(groupNameShortForm, ' ', '')) = ?2) or " +
                        "(lower(replace(groupName, ' ', '')) = ?2 and lower(replace(groupNameShortForm, ' ', '')) = ?1)",
                normalizedA, normalizedB
        ).firstResult();
    }


    public BuyerGroup findByNormalizedGroupName(String groupName) {
        String normalizedName = normalize(groupName);
        return find("replace(lower(groupName), ' ', '') = ?1", normalizedName).firstResult();
    }

    private String normalize(String input) {
        return input == null ? "" : input.toLowerCase().replaceAll("\\s+", "");
    }


    public BuyerGroup findByNormalizedShortForm(String shortForm) {
        String normalizedShortForm = normalize(shortForm);
        return find("replace(lower(groupNameShortForm), ' ', '') = ?1", normalizedShortForm).firstResult();
    }








}






