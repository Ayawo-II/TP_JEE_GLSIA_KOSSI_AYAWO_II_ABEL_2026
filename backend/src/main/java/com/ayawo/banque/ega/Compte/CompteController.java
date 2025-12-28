package com.ayawo.banque.ega.Compte;

import com.ayawo.banque.ega.Client.ClientEntity;
import com.ayawo.banque.ega.Client.ClientRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/compte")
public class CompteController {

    private static final BigDecimal SOLDE_MINIMUM_CREATION = new BigDecimal("10000.00");

    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;

    public CompteController(CompteRepository compteRepository, ClientRepository clientRepository) {
        this.compteRepository = compteRepository;
        this.clientRepository = clientRepository;
    }

    @PostMapping
    public ResponseEntity<?> createCompte(@Valid @RequestBody CompteEntity compte) {

        if (compte.getClient() == null || compte.getClient().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'ID du client est requis");
        }

        Long clientId = compte.getClient().getId();

        ClientEntity existingClient = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        if (compte.getSolde() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le solde initial est obligatoire");
        }

        if (compte.getSolde().compareTo(SOLDE_MINIMUM_CREATION) < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Le solde minimum pour créer un compte est de 10 000");
        }

        String nouveauNumero = "TG-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        compte.setNumeroCompte(nouveauNumero);
        compte.setDateCreation(LocalDate.now());
        compte.setClient(existingClient);

        CompteEntity saved = compteRepository.save(compte);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<CompteEntity>> getAllComptes() {
        List<CompteEntity> comptes = compteRepository.findAll();
        return ResponseEntity.ok(comptes);
    }

    @GetMapping("/{numeroCompte}")
    public ResponseEntity<?> getCompteByNumero(@PathVariable String numeroCompte) {
        CompteEntity compte = compteRepository.findById(numeroCompte)
                .orElseThrow(() -> new RuntimeException("Compte introuvable avec le numéro: " + numeroCompte));

        return ResponseEntity.ok(compte);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CompteEntity>> getComptesByClient(@PathVariable Long clientId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable avec l'ID: " + clientId));

        List<CompteEntity> comptes = compteRepository.findByClientId(clientId);
        return ResponseEntity.ok(comptes);
    }

    @PutMapping("/{numeroCompte}")
    public ResponseEntity<?> updateCompte(
            @PathVariable String numeroCompte,
            @Valid @RequestBody CompteEntity compteUpdates) {

        CompteEntity compte = compteRepository.findById(numeroCompte)
                .orElseThrow(() -> new RuntimeException("Compte introuvable avec le numéro: " + numeroCompte));

        compte.setTypeCompte(compteUpdates.getTypeCompte());

        CompteEntity updated = compteRepository.save(compte);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{numeroCompte}")
    public ResponseEntity<?> deleteCompte(@PathVariable String numeroCompte) {
        if (!compteRepository.existsById(numeroCompte)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Compte introuvable avec le numéro: " + numeroCompte);
        }

        compteRepository.deleteById(numeroCompte);
        return ResponseEntity.noContent().build();
    }

}