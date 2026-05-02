package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.Employee;
import com.app.db.repository.EmployeeRepository;


@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repo;

    @Transactional(transactionManager="jtaTransactionManager")
    public void createEmployee(String name, String dept) {
            Employee emp = new Employee();
            emp.setName(name);
            emp.setDepartment(dept);
            emp.setStatus(0);
            repo.save(emp);    
    }

    @Transactional(transactionManager="jtaTransactionManager")
    public void updateDepartment(String name, String dept) {
        repo.updateDepartmentById(name, dept);
    }

    public List<Employee> getAll() {
        return repo.findAll();
    }
}
