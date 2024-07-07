package kurs.service;

import kurs.model.*;
import kurs.repository.CircleRepository;
import kurs.repository.FigureRepository;
import kurs.repository.RectangleRepository;
import kurs.repository.SquareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportService {

    private static final int BATCH_SIZE = 1000;
    private final ImportStatusFacade importStatusFacade;
    private final FigureRepository figureRepository;
    private final CircleRepository circleRepository;
    private final SquareRepository squareRepository;
    private final RectangleRepository rectangleRepository;

    public ImportStatus startImport(String fileName) {
        return importStatusFacade.saveAndFlush(new ImportStatus(fileName));
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    public ImportStatus findById(int id) {
        return importStatusFacade.findById(id).orElseThrow();
    }

    @Transactional
    @Async("figuresImportExecutor")
    public void importFigures(InputStream inputStream, int id) {
        AtomicInteger counter = new AtomicInteger(0);
        AtomicLong start = new AtomicLong(System.currentTimeMillis());

        importStatusFacade.updateToProcessing(id);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> batch = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                batch.add(line);
                if (batch.size() >= BATCH_SIZE) {
                    processBatch(batch);
                    batch.clear();
                    logProgress(counter, start, id, BATCH_SIZE);
                    System.gc();
                }
            }

            if (!batch.isEmpty()) {
                processBatch(batch);
                logProgress(counter, start, id, batch.size());
                System.gc();
            }
        } catch (Exception e) {
            log.error("Error reading input stream", e);
            importStatusFacade.updateToFail(id, e.getMessage());
            throw new RuntimeException("Failed to import figures", e);
        }
        importStatusFacade.updateToSuccess(id);
    }

    private void logProgress(AtomicInteger counter, AtomicLong start, int id, int batchSize) {
        int progress = counter.addAndGet(batchSize);
        log.info("Imported: {} figures in {} ms for Import ID: {}", progress, (System.currentTimeMillis() - start.get()), id);
        start.set(System.currentTimeMillis());
        importStatusFacade.updateProgress(id, progress);
    }

    private void processBatch(List<String> lines) {
        List<Figure> figuresBatch = lines.stream()
                .map(this::createFigure)
                .collect(Collectors.toList());
        saveBatch(figuresBatch);
    }

    private Figure createFigure(String line) {
        String[] tokens = line.split(";");
        String type = tokens[0];
        switch (type) {
            case "KWADRAT" -> {
                Square square = new Square();
                square.setTyp(type);
                square.setSide(Integer.parseInt(tokens[1]));
                return square;
            }
            case "KOLO" -> {
                Circle circle = new Circle();
                circle.setTyp(type);
                circle.setRadius(Integer.parseInt(tokens[1]));
                return circle;
            }
            case "PROSTOKAT" -> {
                Rectangle rectangle = new Rectangle();
                rectangle.setTyp(type);
                rectangle.setLength(Integer.parseInt(tokens[1]));
                rectangle.setWidth(Integer.parseInt(tokens[2]));
                return rectangle;
            }
            default -> throw new IllegalArgumentException("Unknown figure type: " + type);
        }
    }

    private void saveBatch(List<Figure> figuresBatch) {
        for (Figure figure : figuresBatch) {
            if (figure instanceof Circle) {
                circleRepository.save((Circle) figure);
            } else if (figure instanceof Square) {
                squareRepository.save((Square) figure);
            } else if (figure instanceof Rectangle) {
                rectangleRepository.save((Rectangle) figure);
            } else {
                figureRepository.save(figure);
            }
        }
    }
}
