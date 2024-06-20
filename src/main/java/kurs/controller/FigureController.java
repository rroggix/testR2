package kurs.controller;

import kurs.service.FigureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/figures")
public class FigureController {

    @Autowired
    private FigureService figureService;

    @PostMapping("/import")
    public String importFigures(@RequestParam("file") MultipartFile file) {
        figureService.importFigures(file);
        return "Import zakończony pomyślnie";
    }
}
