package kurs.repository;

import kurs.model.Square;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SquareRepository extends JpaRepository<Square, Integer> {
}
