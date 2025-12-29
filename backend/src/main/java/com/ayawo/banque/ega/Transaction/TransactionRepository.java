package com.ayawo.banque.ega.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity,Long> {

    @Query("SELECT t FROM TransactionEntity t " +
            "WHERE t.compteSource.client.id = :clientId " +
            "   OR t.compteDestination.client.id = :clientId " +
            "ORDER BY t.dateTransaction DESC")
    List<TransactionEntity> findByClientId(@Param("clientId") Long clientId);

}
