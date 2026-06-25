package ch.zhaw.praesto.controller;

import ch.zhaw.praesto.model.CoverLetterRequest;
import ch.zhaw.praesto.model.CvRequest;
import ch.zhaw.praesto.model.DocumentDTO;
import ch.zhaw.praesto.service.GeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Lebenslauf- und Bewerbungsschreiben-Generator (Schüler). Erzeugt eine .docx im Dossier.
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class GeneratorController {

    private final GeneratorService generatorService;

    @PostMapping("/cv")
    public DocumentDTO generateCv(@RequestBody CvRequest request) {
        return generatorService.generateCv(request);
    }

    @PostMapping("/cover-letter")
    public DocumentDTO generateCoverLetter(@RequestBody CoverLetterRequest request) {
        return generatorService.generateCoverLetter(request);
    }
}
