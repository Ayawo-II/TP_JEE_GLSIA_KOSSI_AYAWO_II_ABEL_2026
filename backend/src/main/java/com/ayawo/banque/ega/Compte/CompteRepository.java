package com.ayawo.banque.ega.Compte;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompteRepository extends JpaRepository<CompteEntity, String> {

    List<CompteEntity> findByClientId(Long clientId);
    List<CompteEntity> findByTypeCompte(String typeCompte);
}