package kurs.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Rectangle extends Figure{

    private int length;
    private int width;
}
