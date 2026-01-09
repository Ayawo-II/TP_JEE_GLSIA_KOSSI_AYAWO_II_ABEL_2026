package com.ayawo.banque.ega.controllers;

import com.ayawo.banque.ega.services.ReleveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/releves")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReleveController {

    private final ReleveService releveService;

    /**
     * Générer et télécharger un relevé bancaire
     *
     * GET /api/releves/compte/{numeroCompte}?dateDebut=...&dateFin=...
     */
    @GetMapping("/compte/{numeroCompte}")
    public ResponseEntity<byte[]> genererReleve(
            @PathVariable String numeroCompte,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {

        byte[] pdfBytes = releveService.genererReleve(numeroCompte, dateDebut, dateFin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "releve_" + numeroCompte + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}