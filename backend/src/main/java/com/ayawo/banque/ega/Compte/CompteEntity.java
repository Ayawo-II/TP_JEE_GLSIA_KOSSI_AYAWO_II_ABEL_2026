package com.ayawo.banque.ega.Compte;

import com.ayawo.banque.ega.Client.ClientEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "comptes")
public class CompteEntity {

    @Id
    @Column(length = 34)
    private String numeroCompte;

    @NotNull(message = "Le type de compte est obligatoire")
    @Pattern(regexp = "^(courant|epargne)$", message = "Type de compte invalide")
    private String typeCompte;

    @Column(nullable = false)
    private LocalDate dateCreation;

    @DecimalMin(value = "0.0", message = "Le solde ne peut pas être négatif")
    private BigDecimal solde;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Le client est obligatoire")
    private ClientEntity client;

}
