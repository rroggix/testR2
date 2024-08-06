package kurs.controller;

import kurs.model.ImportStatus;
import kurs.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("api/figure")
@Slf4j
@RequiredArgsConstructor
public class FigureController {

    private final ImportService importService;

    @PostMapping("/import")
    public ResponseEntity<ImportStatus> importFigures(@RequestPart("figure") MultipartFile file) throws IOException {
        log.info("importFigure");
        ImportStatus actualImport = importService.startImport(file.getName());
        importService.importFigures(file.getInputStream(), actualImport.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(actualImport);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportStatus> getImportStatus(@PathVariable int id) throws IOException {
        log.info("getImportStatus({})", id);
        ImportStatus actualImport = importService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(actualImport);
    }
}
