package kurs.service;

import kurs.model.ImportStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportService {

    private static final String INSERT_FIGURE_SQL = "INSERT INTO figure (typ, param1, param2) VALUES (?, ?, ?)";
    private final JdbcTemplate jdbcTemplate;
    private final ImportStatusFacade importStatusFacade;
    private static final int BATCH_SIZE = 1000;

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
            List<String[]> batch = new ArrayList<>();
            reader.lines()
                    .map(line -> line.split(";"))
                    .peek(command -> countTime(counter, start, id))
                    .forEach(args -> {
                        batch.add(args);
                        if (batch.size() >= BATCH_SIZE) {
                            saveFigures(batch);
                            batch.clear();
                        }
                    });

            if (!batch.isEmpty()) {
                saveFigures(batch);
            }
        } catch (Exception e) {
            log.error("Error reading input stream", e);
            importStatusFacade.updateToFail(id, e.getMessage());
            throw new RuntimeException("Failed to import figures", e);
        }
        importStatusFacade.updateToSuccess(id);
    }

    private void countTime(AtomicInteger counter, AtomicLong start, int id) {
        int progress = counter.incrementAndGet();
        if (progress % 10000 == 0) {
            log.info("Imported: {} figures in {} ms", counter.get(), (System.currentTimeMillis() - start.get()));
            start.set(System.currentTimeMillis());
            importStatusFacade.updateProgress(id, progress);
        }
    }

    private void saveFigures(List<String[]> figures) {
        jdbcTemplate.batchUpdate(INSERT_FIGURE_SQL, figures, figures.size(), (ps, args) -> {
            String type = args[0];
            ps.setString(1, type);
            ps.setInt(2, Integer.parseInt(args[1]));
            if ("PROSTOKAT".equals(type)) {
                ps.setInt(3, Integer.parseInt(args[2]));
            } else {
                ps.setNull(3, Types.INTEGER);
            }
        });
    }
}
