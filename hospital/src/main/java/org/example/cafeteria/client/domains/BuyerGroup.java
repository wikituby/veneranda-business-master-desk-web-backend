package org.example.cafeteria.client.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class BuyerGroup extends PanacheEntity {

    @Column(unique = true,nullable = false)
    public String groupName;

    @Column(unique = true,nullable = false)
    public String groupNameShortForm;

    @Column
    public String description;

    @Column
    public String groupAddress;

    @Column
    public String groupEmail;

    @Column
    public String groupContact;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate patientGroupCreationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate patientGroupLastUpdatedDate;


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}






