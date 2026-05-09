package com.app.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.TransactionSndrDtls;

@Repository
public interface TransactionSndrDtlsRepository extends JpaRepository<TransactionSndrDtls, Long> {

    @Query(value = """
                SELECT *
                FROM TRANSACTION_SNDR
                 FETCH FIRST :batchSize ROWS ONLY
                FOR UPDATE SKIP LOCKED
                """, nativeQuery = true)
    @Transactional(transactionManager="jtaTransactionManager")
    List<TransactionSndrDtls> fetchAndLockTransactions(int batchSize);

}
