package kurs.repository;

import kurs.model.Circle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CircleRepository extends JpaRepository<Circle, Integer> {
}
