package kurs.controller;

import kurs.service.FigureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/figury")
public class FigureController {

    private static final Logger logger = LoggerFactory.getLogger(FigureController.class);

    @Autowired
    private FigureService figuryService;

    @PostMapping("/import")
    public ResponseEntity<String> importCSV(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("Received file: " + file.getOriginalFilename());
            figuryService.importCSV(file);
            return new ResponseEntity<>("File imported successfully", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to import file", e);
            return new ResponseEntity<>("Failed to import file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
