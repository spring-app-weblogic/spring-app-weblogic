package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.Department;
import com.app.db.repository.DepartmentRepository;

@Component
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Transactional(transactionManager="jtaTransactionManager")
    public void createDepartment(String dept) {
        Department department = new Department();
        department.setName(dept);
        departmentRepository.save(department);
    }
}
