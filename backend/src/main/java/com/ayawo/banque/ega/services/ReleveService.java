package com.ayawo.banque.ega.services;

import com.ayawo.banque.ega.entities.CompteEntity;
import com.ayawo.banque.ega.entities.TransactionEntity;
import com.ayawo.banque.ega.enums.TypeTransaction;
import com.ayawo.banque.ega.exceptions.compte.CompteNotFoundException;
import com.ayawo.banque.ega.repositories.CompteRepository;
import com.ayawo.banque.ega.repositories.TransactionRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReleveService {

    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_SIMPLE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Générer un relevé bancaire en PDF
     */
    @Transactional(readOnly = true)
    public byte[] genererReleve(String numeroCompte, LocalDateTime dateDebut, LocalDateTime dateFin) {
        log.info("Génération du relevé pour le compte {} du {} au {}", numeroCompte, dateDebut, dateFin);

        // Récupérer le compte
        CompteEntity compte = compteRepository.findByNumeroCompte(numeroCompte)
                .orElseThrow(() -> new CompteNotFoundException(numeroCompte));

        // Récupérer les transactions de la période
        List<TransactionEntity> transactions = transactionRepository
                .findByNumeroCompteAndDateBetween(numeroCompte, dateDebut, dateFin);

        // Calculer le solde initial (solde actuel - toutes les transactions après la période)
        BigDecimal soldeActuel = compte.getSolde();
        BigDecimal soldeFinal = soldeActuel;
        BigDecimal soldeInitial = calculerSoldeInitial(compte, dateDebut);

        // Générer le PDF
        return genererPDF(compte, transactions, dateDebut, dateFin, soldeInitial, soldeFinal);
    }

    /**
     * Calculer le solde initial à une date donnée
     */
    private BigDecimal calculerSoldeInitial(CompteEntity compte, LocalDateTime dateDebut) {
        BigDecimal soldeActuel = compte.getSolde();

        // Récupérer toutes les transactions après la date de début
        List<TransactionEntity> transactionsApres = transactionRepository
                .findByNumeroCompteAndDateBetween(compte.getNumeroCompte(), dateDebut, LocalDateTime.now());

        // Soustraire/Ajouter les transactions pour retrouver le solde initial
        for (TransactionEntity transaction : transactionsApres) {
            if (transaction.getCompteSource().getNumeroCompte().equals(compte.getNumeroCompte())) {
                // C'était une sortie d'argent, on la rajoute pour retrouver le solde d'avant
                if (transaction.getType() == TypeTransaction.RETRAIT || transaction.getType() == TypeTransaction.VIREMENT) {
                    soldeActuel = soldeActuel.add(transaction.getMontant());
                } else if (transaction.getType() == TypeTransaction.DEPOT) {
                    soldeActuel = soldeActuel.subtract(transaction.getMontant());
                }
            }

            if (transaction.getCompteDestination() != null &&
                    transaction.getCompteDestination().getNumeroCompte().equals(compte.getNumeroCompte())) {
                // C'était une entrée d'argent (virement reçu), on la soustrait
                soldeActuel = soldeActuel.subtract(transaction.getMontant());
            }
        }

        return soldeActuel;
    }

    /**
     * Générer le PDF du relevé
     */
    private byte[] genererPDF(CompteEntity compte, List<TransactionEntity> transactions,
                              LocalDateTime dateDebut, LocalDateTime dateFin,
                              BigDecimal soldeInitial, BigDecimal soldeFinal) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // En-tête
            ajouterEntete(document, compte, dateDebut, dateFin);

            // Informations du compte
            ajouterInfosCompte(document, compte, soldeInitial, soldeFinal);

            // Tableau des transactions
            ajouterTableauTransactions(document, transactions, compte.getNumeroCompte());

            // Pied de page
            ajouterPiedDePage(document);

            document.close();

            log.info("Relevé généré avec succès pour le compte {}", compte.getNumeroCompte());

        } catch (Exception e) {
            log.error("Erreur lors de la génération du relevé PDF", e);
            throw new RuntimeException("Erreur lors de la génération du relevé", e);
        }

        return baos.toByteArray();
    }

    /**
     * Ajouter l'en-tête du relevé
     */
    private void ajouterEntete(Document document, CompteEntity compte,
                               LocalDateTime dateDebut, LocalDateTime dateFin) {

        // Titre
        Paragraph titre = new Paragraph("RELEVÉ BANCAIRE")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(titre);

        // Nom de la banque
        Paragraph banque = new Paragraph("BANQUE EGA")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(banque);

        // Période
        Paragraph periode = new Paragraph(
                String.format("Période du %s au %s",
                        dateDebut.format(DATE_SIMPLE),
                        dateFin.format(DATE_SIMPLE)))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(periode);
    }

    /**
     * Ajouter les informations du compte
     */
    private void ajouterInfosCompte(Document document, CompteEntity compte,
                                    BigDecimal soldeInitial, BigDecimal soldeFinal) {

        document.add(new Paragraph("INFORMATIONS DU COMPTE")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(10));

        Table table = new Table(2);
        table.setWidth(UnitValue.createPercentValue(100));

        // Titulaire
        table.addCell(new Cell().add(new Paragraph("Titulaire :").setBold()));
        table.addCell(new Cell().add(new Paragraph(compte.getProprietaire().getNomComplet())));

        // Numéro de compte
        table.addCell(new Cell().add(new Paragraph("Numéro de compte :").setBold()));
        table.addCell(new Cell().add(new Paragraph(compte.getNumeroCompte())));

        // Type de compte
        table.addCell(new Cell().add(new Paragraph("Type de compte :").setBold()));
        table.addCell(new Cell().add(new Paragraph(compte.getTypeCompte().toString())));

        // Solde initial
        table.addCell(new Cell().add(new Paragraph("Solde initial :").setBold()));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f FCFA", soldeInitial))));

        // Solde final
        table.addCell(new Cell().add(new Paragraph("Solde final :").setBold()));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f FCFA", soldeFinal)).setBold()));

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Ajouter le tableau des transactions
     */
    private void ajouterTableauTransactions(Document document, List<TransactionEntity> transactions,
                                            String numeroCompte) {

        document.add(new Paragraph("LISTE DES OPÉRATIONS")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10));

        if (transactions.isEmpty()) {
            document.add(new Paragraph("Aucune opération sur cette période.")
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER));
            return;
        }

        // Tableau avec 5 colonnes
        float[] columnWidths = {15f, 20f, 35f, 15f, 15f};
        Table table = new Table(columnWidths);
        table.setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Type").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Débit").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Crédit").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        // Lignes de transactions
        for (TransactionEntity transaction : transactions) {
            table.addCell(new Cell().add(new Paragraph(transaction.getDate().format(DATE_FORMATTER)))
                    .setFontSize(10));

            table.addCell(new Cell().add(new Paragraph(transaction.getType().toString()))
                    .setFontSize(10));

            // Débit ou Crédit
            boolean estDebit = transaction.getCompteSource().getNumeroCompte().equals(numeroCompte) &&
                    (transaction.getType() == TypeTransaction.RETRAIT ||
                            transaction.getType() == TypeTransaction.VIREMENT);

            if (estDebit) {
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", transaction.getMontant())))
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(ColorConstants.RED));
                table.addCell(new Cell().add(new Paragraph("-"))
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT));
            } else {
                table.addCell(new Cell().add(new Paragraph("-"))
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", transaction.getMontant())))
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(ColorConstants.GREEN));
            }
        }

        document.add(table);
    }

    /**
     * Ajouter le pied de page
     */
    private void ajouterPiedDePage(Document document) {
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Banque EGA - Tous droits réservés")
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

        document.add(new Paragraph("Date d'édition : " + LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER));
    }
}