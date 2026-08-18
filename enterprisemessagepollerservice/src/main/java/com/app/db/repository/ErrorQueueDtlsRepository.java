package com.app.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.db.entity.ErrorQueueDtls;

@Repository
public interface ErrorQueueDtlsRepository extends JpaRepository<ErrorQueueDtls, Long> {

    long countByMessageHash(String hash);

}
