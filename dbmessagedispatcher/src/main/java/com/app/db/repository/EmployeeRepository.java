package com.app.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Modifying
    @Query("UPDATE Employee e SET e.department = :dept WHERE e.name = :name")
    int updateDepartmentById(@Param("name") String name, @Param("dept") String dept);

    @Query(value = """
                SELECT *
                FROM EMPLOYEE
                WHERE status = 0
                 FETCH FIRST :batchSize ROWS ONLY
                FOR UPDATE SKIP LOCKED
                """, nativeQuery = true)
    @Transactional(transactionManager="jtaTransactionManager")
    List<Employee> fetchAndLockEmployees(@Param("batchSize") int batchSize);

    @Modifying
    @Query("UPDATE Employee e SET e.status = :status WHERE e.id IN (:ids)")
    @Transactional(transactionManager="jtaTransactionManager")
    int updateStatusForIds(@Param("status") int status,
                           @Param("ids") List<Long> ids);

}
