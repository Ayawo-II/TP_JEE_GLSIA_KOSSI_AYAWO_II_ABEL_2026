package com.ayawo.banque.ega.dto.releve;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleveRequestDTO {

    @NotBlank(message = "Le numéro de compte est obligatoire")
    private String numeroCompte;

    @NotNull(message = "La date de début est obligatoire")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateFin;
}