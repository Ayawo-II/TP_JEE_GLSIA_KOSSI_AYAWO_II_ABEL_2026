package com.ayawo.banque.ega.services;

import com.ayawo.banque.ega.entities.ClientEntity;
import com.ayawo.banque.ega.entities.CompteEntity;
import com.ayawo.banque.ega.entities.TransactionEntity;
import com.ayawo.banque.ega.enums.TypeTransaction;
import com.ayawo.banque.ega.repositories.ClientRepository;
import com.ayawo.banque.ega.repositories.CompteRepository;
import com.ayawo.banque.ega.repositories.TransactionRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
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
@Transactional(readOnly = true)
public class ReleveService {

    private final ClientRepository clientRepository;
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter DATE_SIMPLE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===================== MÉTHODE PRINCIPALE =====================

    public byte[] genererReleveGlobalClient(
            Long clientId,
            LocalDateTime dateDebut,
            LocalDateTime dateFin) {

        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        List<CompteEntity> comptes =
                compteRepository.findByProprietaireId(clientId);

        List<TransactionEntity> transactions =
                transactionRepository.findByClientIdAndDateBetween(
                        clientId, dateDebut, dateFin);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            ajouterEntete(document, client, dateDebut, dateFin);
            ajouterTableauComptes(document, comptes);
            ajouterTableauOperations(document, transactions);

            document.close();
        } catch (Exception e) {
            log.error("Erreur génération relevé", e);
            throw new RuntimeException("Erreur génération PDF", e);
        }

        return baos.toByteArray();
    }

    // ===================== EN-TÊTE =====================

    private void ajouterEntete(Document doc,
                               ClientEntity client,
                               LocalDateTime debut,
                               LocalDateTime fin) {

        doc.add(new Paragraph("RELEVÉ BANCAIRE")
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("BANQUE EGA")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        doc.add(new Paragraph("Client : " + client.getNomComplet()));
        doc.add(new Paragraph("Adresse : " + client.getAdresse()));
        doc.add(new Paragraph("Période : du "
                + debut.format(DATE_SIMPLE)
                + " au "
                + fin.format(DATE_SIMPLE)));

        doc.add(new Paragraph("\n"));
    }

    // ===================== TABLEAU COMPTES =====================

    private void ajouterTableauComptes(Document doc,
                                       List<CompteEntity> comptes) {

        doc.add(new Paragraph("LISTE DES COMPTES")
                .setBold()
                .setFontSize(14));

        Table table = new Table(new float[]{30, 30, 30});
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell("Numéro de compte");
        table.addHeaderCell("Type de compte");
        table.addHeaderCell("Solde");

        for (CompteEntity compte : comptes) {
            table.addCell(compte.getNumeroCompte());
            table.addCell(compte.getTypeCompte().name());
            table.addCell(compte.getSolde() + " FCFA");
        }

        doc.add(table);
        doc.add(new Paragraph("\n"));
    }

    // ===================== TABLEAU OPÉRATIONS =====================

    private void ajouterTableauOperations(Document doc,
                                          List<TransactionEntity> transactions) {

        doc.add(new Paragraph("HISTORIQUE DES OPÉRATIONS")
                .setBold()
                .setFontSize(14));

        Table table = new Table(new float[]{20, 20, 30, 15, 15});
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell("Date");
        table.addHeaderCell("N° Compte");
        table.addHeaderCell("Opération");
        table.addHeaderCell("Débit");
        table.addHeaderCell("Crédit");

        for (TransactionEntity t : transactions) {

            switch (t.getType()) {

                case DEPOT -> ajouterLigne(
                        table,
                        t,
                        t.getCompteSource().getNumeroCompte(),
                        "Dépôt",
                        null,
                        t.getMontant()
                );

                case RETRAIT -> ajouterLigne(
                        table,
                        t,
                        t.getCompteSource().getNumeroCompte(),
                        "Retrait",
                        t.getMontant(),
                        null
                );

                case VIREMENT -> {
                    // Débit compte source
                    ajouterLigne(
                            table,
                            t,
                            t.getCompteSource().getNumeroCompte(),
                            "Virement sortant",
                            t.getMontant(),
                            null
                    );

                    // Crédit compte destination
                    ajouterLigne(
                            table,
                            t,
                            t.getCompteDestination().getNumeroCompte(),
                            "Virement entrant",
                            null,
                            t.getMontant()
                    );
                }
            }
        }

        doc.add(table);
    }

    // ===================== LIGNE OPÉRATION =====================

    private void ajouterLigne(Table table,
                              TransactionEntity t,
                              String numeroCompte,
                              String operation,
                              BigDecimal debit,
                              BigDecimal credit) {

        table.addCell(t.getDate().format(DATE_FORMAT));
        table.addCell(numeroCompte);
        table.addCell(operation);
        table.addCell(debit == null ? "-" : debit + " FCFA");
        table.addCell(credit == null ? "-" : credit + " FCFA");
    }
}
