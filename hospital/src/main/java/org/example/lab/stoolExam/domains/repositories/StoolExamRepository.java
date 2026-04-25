package org.example.lab.stoolExam.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.lab.stoolExam.domains.StoolExam;

@ApplicationScoped
public class StoolExamRepository implements PanacheRepository<StoolExam> {
}






