package com.app.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.db.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

}
