package com.app.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.db.entity.FailMessage;

@Repository
public interface FailMessageRepository extends JpaRepository<FailMessage, Long>{

    int countByNameAndDepartment(String name, String department);

}
