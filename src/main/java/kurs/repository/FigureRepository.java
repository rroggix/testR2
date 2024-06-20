package kurs.repository;

import kurs.model.Figure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FigureRepository extends JpaRepository<Figure, Integer> {
}
