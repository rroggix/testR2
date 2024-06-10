package kurs.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public  class Figure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @SequenceGenerator(name = "figureIdGenerator", sequenceName = "figure_seq", initialValue = 1, allocationSize = 100)
    private Long id;

    @Column(nullable = false)
    private String typ;

    private Double param1;
    private Double param2;
    private Double param3;

}
