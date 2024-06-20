package kurs.service;

import com.opencsv.CSVReader;
import kurs.model.Circle;
import kurs.model.Rectangle;
import kurs.model.Square;
import kurs.repository.CircleRepository;
import kurs.repository.RectangleRepository;
import kurs.repository.SquareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class FigureService {

    @Autowired
    private SquareRepository squareRepository;

    @Autowired
    private CircleRepository circleRepository;

    @Autowired
    private RectangleRepository rectangleRepository;

    @Transactional
    public void importFigures(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                String typ = line[0];
                switch (typ.toUpperCase()) {
                    case "KWADRAT":
                        Square square = new Square();
                        square.setTyp(typ);
                        square.setSide(Integer.parseInt(line[1]));
                        squareRepository.save(square);
                        break;
                    case "KOLO":
                        Circle circle = new Circle();
                        circle.setTyp(typ);
                        circle.setRadius(Integer.parseInt(line[1]));
                        circleRepository.save(circle);
                        break;
                    case "PROSTOKAT":
                        Rectangle rectangle = new Rectangle();
                        rectangle.setTyp(typ);
                        rectangle.setLength(Integer.parseInt(line[1]));
                        rectangle.setWidth(Integer.parseInt(line[2]));
                        rectangleRepository.save(rectangle);
                        break;
                    default:
                        throw new IllegalArgumentException("Nieznany typ figury: " + typ);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import figures", e);
        }
    }
}
