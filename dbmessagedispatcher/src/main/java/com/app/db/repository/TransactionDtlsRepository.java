package com.app.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.db.entity.TransactionDtls;

@Repository
public interface TransactionDtlsRepository extends JpaRepository<TransactionDtls, Long> {

    TransactionDtls findByTransactionId(String transactionId);

}
