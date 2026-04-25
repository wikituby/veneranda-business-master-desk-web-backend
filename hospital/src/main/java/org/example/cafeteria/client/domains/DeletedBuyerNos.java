package org.example.cafeteria.client.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class DeletedBuyerNos extends PanacheEntity {

    @Column
    public int deletedPatientNo;


}








