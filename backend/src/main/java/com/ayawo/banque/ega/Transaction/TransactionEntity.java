package com.ayawo.banque.ega.Transaction;

import com.ayawo.banque.ega.Compte.CompteEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date est obligatoire")
    @PastOrPresent(message = "La date ne peut pas être dans le futur")
    private LocalDateTime dateTransaction;

    @NotNull(message = "Le type de transaction est obligatoire")
    @Pattern(
            regexp = "^(depot|retrait|virement)$",
            message = "Le type de transaction invalide"
    )
    private String typeTransaction;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal montant;

    @NotNull(message = "Le compte source est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_source_id", nullable = false)
    private CompteEntity compteSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_destination_id")
    private CompteEntity compteDestination;

}
