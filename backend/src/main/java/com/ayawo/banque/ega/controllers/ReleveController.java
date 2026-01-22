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
     * Générer et télécharger un relevé bancaire GLOBAL
     * (tous les comptes + toutes les opérations)
     *
     * GET /releves/client/{clientId}
     * ?dateDebut=2025-01-01T00:00:00
     * &dateFin=2025-01-31T23:59:59
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<byte[]> genererReleveGlobalClient(
            @PathVariable Long clientId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateDebut,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateFin
    ) {

        log.info("📄 Génération relevé client {} du {} au {}",
                clientId, dateDebut, dateFin);

        byte[] pdf = releveService.genererReleveGlobalClient(
                clientId, dateDebut, dateFin
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "attachment",
                "releve_client_" + clientId + ".pdf"
        );
        headers.setContentLength(pdf.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
