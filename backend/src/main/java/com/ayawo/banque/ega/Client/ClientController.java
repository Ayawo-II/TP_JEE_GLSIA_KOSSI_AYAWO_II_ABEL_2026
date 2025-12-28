package com.ayawo.banque.ega.Client;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientController(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<ClientEntity> createClient(@Valid @RequestBody ClientEntity client){

        // 1. Vérifier si email existe (avec Optional)
        ClientEntity existingClient = clientRepository.findByEmail(client.getEmail());
        if(existingClient != null){
            throw new IllegalArgumentException("Cette adresse email est déjà utilisée");
        }

        String hashedPassword = passwordEncoder.encode(client.getPassword());
        client.setPassword(hashedPassword);

        client.setRole("client");

        ClientEntity clientCreated = clientRepository.save(client);

        clientCreated.setPassword(null);

        return new ResponseEntity<>(clientCreated, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ClientEntity>> getAllClients() {
        return new ResponseEntity<>(clientRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientEntity> getClientById(@PathVariable Long id){
        Optional<ClientEntity> client = clientRepository.findById(id);
        if(client.isPresent()){
            return new ResponseEntity<>(client.get(), HttpStatus.OK);
        } else {
            throw new ClientNotFoundException("Ce client n'existe pas");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientEntity> updateClient(@PathVariable Long id, @RequestBody ClientEntity clientDetails){
        Optional<ClientEntity> client = clientRepository.findById(id);

        if(client.isPresent()){
            ClientEntity existingClient = client.get();

            existingClient.setEmail(clientDetails.getEmail());
            existingClient.setPassword(clientDetails.getPassword());
            existingClient.setNom(clientDetails.getNom());
            existingClient.setPrenom(clientDetails.getPrenom());
            existingClient.setDateNaiss(clientDetails.getDateNaiss());
            existingClient.setSexe(clientDetails.getSexe());
            existingClient.setAdresse(clientDetails.getAdresse());
            existingClient.setNumTel(clientDetails.getNumTel());
            existingClient.setNationalite(clientDetails.getNationalite());

            ClientEntity clientUpdated = clientRepository.save(existingClient);

            return new ResponseEntity<>(clientUpdated, HttpStatus.OK);
        }
        throw new ClientNotFoundException("Ce client n'existe pas");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        Optional<ClientEntity> client = clientRepository.findById(id);
        if (client.isPresent()) {
            clientRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        throw new ClientNotFoundException("Ce client n'existe pas");

    }

}
