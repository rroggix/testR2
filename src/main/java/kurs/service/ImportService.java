package kurs.service;

import kurs.model.*;
import kurs.repository.FigureRepository;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportService {

    private static final int BATCH_SIZE = 10000;
    private final ImportStatusFacade importStatusFacade;
    private final FigureRepository figureRepository;

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
            String line;
            List<Figure> batch = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                Figure figure = createFigure(parts);
                if (figure != null) {
                    batch.add(figure);
                    counter.incrementAndGet();
                }

                if (batch.size() >= BATCH_SIZE) {
                    figureRepository.saveAll(batch);
                    batch.clear();
                    logProgress(counter, start, id);
                }
            }

            if (!batch.isEmpty()) {
                figureRepository.saveAll(batch);
            }
        } catch (Exception e) {
            log.error("Error reading input stream", e);
            importStatusFacade.updateToFail(id, e.getMessage());
            throw new RuntimeException("Failed to import figures", e);
        }
        importStatusFacade.updateToSuccess(id);
    }

    private void logProgress(AtomicInteger counter, AtomicLong start, int id) {
        int progress = counter.get();
        log.info("Imported: {} in {} ms", progress, (System.currentTimeMillis() - start.get()));
        start.set(System.currentTimeMillis());
        importStatusFacade.updateProgress(id, progress);
    }

    private Figure createFigure(String[] parts) {
        String type = parts[0];
        return switch (type) {
            case "KWADRAT" -> new Square(Integer.parseInt(parts[1]));
            case "KOLO" -> new Circle(Integer.parseInt(parts[1]));
            case "PROSTOKAT" -> new Rectangle(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            default -> null;
        };
    }
}