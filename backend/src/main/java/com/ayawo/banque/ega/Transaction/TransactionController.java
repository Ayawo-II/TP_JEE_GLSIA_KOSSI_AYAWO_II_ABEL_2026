package com.ayawo.banque.ega.Transaction;

import com.ayawo.banque.ega.Client.ClientRepository;
import com.ayawo.banque.ega.Compte.CompteEntity;
import com.ayawo.banque.ega.Compte.CompteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;

    public TransactionController(TransactionRepository transactionRepository,
                                 CompteRepository compteRepository, ClientRepository clientRepository) {
        this.transactionRepository = transactionRepository;
        this.compteRepository = compteRepository;
        this.clientRepository = clientRepository;
    }

    @PostMapping("/depot/{numeroCompte}")
    public ResponseEntity<?> effectuerDepot(
            @PathVariable String numeroCompte,
            @RequestBody Map<String, Object> request) {

        if (!request.containsKey("montant")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le champ 'montant' est obligatoire");
        }

        BigDecimal montant;
        try {
            Object montantObj = request.get("montant");

            if (montantObj instanceof Integer) {
                montant = new BigDecimal((Integer) montantObj);
            } else if (montantObj instanceof Double) {
                montant = BigDecimal.valueOf((Double) montantObj);
            } else if (montantObj instanceof String) {
                montant = new BigDecimal((String) montantObj);
            } else if (montantObj instanceof BigDecimal) {
                montant = (BigDecimal) montantObj;
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Le montant doit être un nombre valide");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Format de montant invalide: " + e.getMessage());
        }

        Optional<CompteEntity> compteOpt = compteRepository.findById(numeroCompte);

        if (!compteOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Compte introuvable: " + numeroCompte);
        }

        CompteEntity compte = compteOpt.get();

        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le montant doit être supérieur à 0");
        }

        BigDecimal ancienSolde = compte.getSolde();
        BigDecimal nouveauSolde = ancienSolde.add(montant);
        compte.setSolde(nouveauSolde);

        compteRepository.save(compte);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setTypeTransaction("depot");
        transaction.setMontant(montant);
        transaction.setDateTransaction(LocalDateTime.now());
        transaction.setCompteSource(compte);
        transaction.setCompteDestination(null);

        TransactionEntity savedTransaction = transactionRepository.save(transaction);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Dépôt effectué avec succès");
        response.put("transaction", savedTransaction);
        response.put("ancienSolde", ancienSolde);
        response.put("nouveauSolde", nouveauSolde);
        response.put("numeroCompte", numeroCompte);
        response.put("montant", montant);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/retrait/{numeroCompte}")
    public ResponseEntity<?> effectuerRetrait(
            @PathVariable String numeroCompte,
            @RequestBody Map<String, Object> request) {

        if (!request.containsKey("montant")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le champ 'montant' est obligatoire");
        }

        BigDecimal montant;
        try {
            Object montantObj = request.get("montant");

            if (montantObj instanceof Integer) {
                montant = new BigDecimal((Integer) montantObj);
            } else if (montantObj instanceof Double) {
                montant = BigDecimal.valueOf((Double) montantObj);
            } else if (montantObj instanceof String) {
                montant = new BigDecimal((String) montantObj);
            } else if (montantObj instanceof BigDecimal) {
                montant = (BigDecimal) montantObj;
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Le montant doit être un nombre valide");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Format de montant invalide: " + e.getMessage());
        }

        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le montant doit être supérieur à 0");
        }

        Optional<CompteEntity> compteOpt = compteRepository.findById(numeroCompte);

        if (!compteOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Compte introuvable: " + numeroCompte);
        }

        CompteEntity compte = compteOpt.get();

        BigDecimal soldeActuel = compte.getSolde();

        if (soldeActuel.compareTo(montant) < 0) {
            Map<String, Object> erreur = new HashMap<>();
            erreur.put("message", "Solde insuffisant");
            erreur.put("soldeActuel", soldeActuel);
            erreur.put("montantDemande", montant);
            erreur.put("deficit", montant.subtract(soldeActuel));
            erreur.put("numeroCompte", numeroCompte);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
        }

        BigDecimal nouveauSolde = soldeActuel.subtract(montant);
        compte.setSolde(nouveauSolde);
        compteRepository.save(compte);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setTypeTransaction("retrait");
        transaction.setMontant(montant);
        transaction.setDateTransaction(LocalDateTime.now());
        transaction.setCompteSource(compte);
        transaction.setCompteDestination(null);

        TransactionEntity savedTransaction = transactionRepository.save(transaction);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Retrait effectué avec succès");
        response.put("transaction", savedTransaction);
        response.put("ancienSolde", soldeActuel);
        response.put("nouveauSolde", nouveauSolde);
        response.put("numeroCompte", numeroCompte);
        response.put("montant", montant);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/virement")
    public ResponseEntity<?> effectuerVirement(@RequestBody Map<String, Object> request) {

        if (!request.containsKey("compteSource")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le champ 'compteSource' est obligatoire");
        }

        if (!request.containsKey("compteDestination")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le champ 'compteDestination' est obligatoire");
        }

        if (!request.containsKey("montant")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le champ 'montant' est obligatoire");
        }

        String numeroCompteSource, numeroCompteDestination;

        try {
            Object sourceObj = request.get("compteSource");
            Object destObj = request.get("compteDestination");

            if (sourceObj instanceof Map) {
                Map<?, ?> sourceMap = (Map<?, ?>) sourceObj;
                numeroCompteSource = sourceMap.get("numeroCompte").toString();
            } else {
                numeroCompteSource = sourceObj.toString();
            }

            if (destObj instanceof Map) {
                Map<?, ?> destMap = (Map<?, ?>) destObj;
                numeroCompteDestination = destMap.get("numeroCompte").toString();
            } else {
                numeroCompteDestination = destObj.toString();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Format des comptes invalide: " + e.getMessage());
        }

        BigDecimal montant;
        try {
            Object montantObj = request.get("montant");

            if (montantObj instanceof Integer) {
                montant = new BigDecimal((Integer) montantObj);
            } else if (montantObj instanceof Double) {
                montant = BigDecimal.valueOf((Double) montantObj);
            } else if (montantObj instanceof String) {
                montant = new BigDecimal((String) montantObj);
            } else if (montantObj instanceof BigDecimal) {
                montant = (BigDecimal) montantObj;
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Le montant doit être un nombre valide");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Format de montant invalide: " + e.getMessage());
        }

        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le montant doit être supérieur à 0");
        }

        if (numeroCompteSource.equals(numeroCompteDestination)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le compte source et le compte destination doivent être différents");
        }

        Optional<CompteEntity> compteSourceOpt = compteRepository.findById(numeroCompteSource);
        if (!compteSourceOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Compte source introuvable: " + numeroCompteSource);
        }
        CompteEntity compteSource = compteSourceOpt.get();

        Optional<CompteEntity> compteDestOpt = compteRepository.findById(numeroCompteDestination);
        if (!compteDestOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Compte destination introuvable: " + numeroCompteDestination);
        }
        CompteEntity compteDestination = compteDestOpt.get();

        BigDecimal soldeSource = compteSource.getSolde();
        if (soldeSource.compareTo(montant) < 0) {
            Map<String, Object> erreur = new HashMap<>();
            erreur.put("message", "Solde insuffisant sur le compte source");
            erreur.put("compteSource", numeroCompteSource);
            erreur.put("soldeSource", soldeSource);
            erreur.put("montantDemande", montant);
            erreur.put("deficit", montant.subtract(soldeSource));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
        }

        BigDecimal nouveauSoldeSource = soldeSource.subtract(montant);
        compteSource.setSolde(nouveauSoldeSource);

        BigDecimal soldeDestination = compteDestination.getSolde();
        BigDecimal nouveauSoldeDestination = soldeDestination.add(montant);
        compteDestination.setSolde(nouveauSoldeDestination);

        compteRepository.save(compteSource);
        compteRepository.save(compteDestination);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setTypeTransaction("virement");
        transaction.setMontant(montant);
        transaction.setDateTransaction(LocalDateTime.now());
        transaction.setCompteSource(compteSource);
        transaction.setCompteDestination(compteDestination);

        TransactionEntity savedTransaction = transactionRepository.save(transaction);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Virement effectué avec succès");
        response.put("transaction", savedTransaction);
        response.put("compteSource", Map.of(
                "numeroCompte", numeroCompteSource,
                "ancienSolde", soldeSource,
                "nouveauSolde", nouveauSoldeSource
        ));
        response.put("compteDestination", Map.of(
                "numeroCompte", numeroCompteDestination,
                "ancienSolde", soldeDestination,
                "nouveauSolde", nouveauSoldeDestination
        ));
        response.put("montant", montant);
        response.put("date", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getTransactionsDuClient(
            @PathVariable Long clientId) {

        if (!clientRepository.existsById(clientId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Client introuvable avec l'ID: " + clientId);
        }

        List<String> numerosComptes = compteRepository.findNumerosCompteByClientId(clientId);

        if (numerosComptes.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ce client n'a pas de comptes");
            response.put("clientId", clientId);
            response.put("transactions", List.of());
            return ResponseEntity.ok(response);
        }

        List<TransactionEntity> transactions = transactionRepository
                .findByClientId(clientId);

        Map<String, Object> response = new HashMap<>();
        response.put("clientId", clientId);
        response.put("comptesDuClient", numerosComptes);
        response.put("nombreComptes", numerosComptes.size());
        response.put("transactions", transactions);

        return ResponseEntity.ok(response);
    }


}