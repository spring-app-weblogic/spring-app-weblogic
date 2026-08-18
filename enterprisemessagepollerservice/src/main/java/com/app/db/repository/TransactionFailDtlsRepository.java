package com.app.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.db.entity.TransactionFailDtls;

@Repository
public interface TransactionFailDtlsRepository extends JpaRepository<TransactionFailDtls, Long> {

    long countByTransactionId(String transactionId);
}
