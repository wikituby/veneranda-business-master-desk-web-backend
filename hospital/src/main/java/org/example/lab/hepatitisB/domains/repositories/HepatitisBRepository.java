package org.example.lab.hepatitisB.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab.hepatitisB.domains.HepatitisB;

@ApplicationScoped
public class HepatitisBRepository implements PanacheRepository<HepatitisB> {
}






