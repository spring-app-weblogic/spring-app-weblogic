package com.app.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.db.entity.TransactionSndrDtls;

@Repository
public interface TransactionSndrDtlsRepository extends JpaRepository<TransactionSndrDtls, Long> {

}
