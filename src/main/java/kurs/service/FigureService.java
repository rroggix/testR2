package kurs.service;

import jakarta.transaction.Transactional;
import kurs.model.Figure;
import kurs.repository.FigureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class FigureService {

    private static final Logger logger = LoggerFactory.getLogger(FigureService.class);

    @Autowired
    private FigureRepository repository;

    @Transactional
    public void importCSV(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<Figure> figuryList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                figuryList.add(mapToFigure(line));
            }
            repository.saveAll(figuryList);
        } catch (Exception e) {
            logger.error("Failed to import CSV file", e);
            throw new RuntimeException("Failed to import CSV file: " + e.getMessage(), e);
        }
    }

    private Figure mapToFigure(String line) {
        String[] fields = line.split(";");
        Figure figura = new Figure();
        figura.setTyp(fields[0]);
        figura.setParam1(Double.parseDouble(fields[1]));
        if (fields.length > 2) figura.setParam2(Double.parseDouble(fields[2]));
        if (fields.length > 3) figura.setParam3(Double.parseDouble(fields[3]));
        return figura;
    }
}
