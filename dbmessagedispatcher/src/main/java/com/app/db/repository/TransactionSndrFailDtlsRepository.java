package com.app.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.db.entity.TransactionSndrFailDtls;

@Repository
public interface TransactionSndrFailDtlsRepository extends JpaRepository<TransactionSndrFailDtls, Long> {

    long countByTransactionId(String transactionId);
}
