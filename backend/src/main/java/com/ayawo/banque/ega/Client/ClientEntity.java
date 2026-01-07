package com.ayawo.banque.ega.Client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "clients")
@JsonPropertyOrder({
        "id",
        "nom",
        "prenom",
        "email",
        "dateNaiss",
        "sexe",
        "nationalite",
        "numTel",
        "adresse",
        "role",
        "password"
})
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @JsonIgnore
    private String password;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    private String prenom;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaiss;

    @NotBlank(message = "Le sexe est obligatoire")
    @Pattern(regexp = "^(masculin|feminin)$", message = "Le sexe doit être masculin ou féminin")
    private String sexe;

    private String adresse;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String numTel;

    @NotBlank(message = "La nationalité est obligatoire")
    private String nationalite;

    private String role = "client";

}
