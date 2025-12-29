package com.ayawo.banque.ega.Compte;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompteRepository extends JpaRepository<CompteEntity, String> {

    List<CompteEntity> findByClientId(Long clientId);
    List<CompteEntity> findByTypeCompte(String typeCompte);

    @Query("SELECT c.numeroCompte FROM CompteEntity c WHERE c.client.id = :clientId")
    List<String> findNumerosCompteByClientId(@Param("clientId") Long clientId);
}