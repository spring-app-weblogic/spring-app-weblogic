package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageProcessor {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private FailMessageProcessor failMessageProcessor;

    @Transactional(transactionManager="jtaTransactionManager")
    public void processMessage(String name, String dept, String saveOrUpdate) {
        if(failMessageProcessor.searchFailedMessage(name, dept)) {
            System.out.println("Message already present in failed message table, hence ignoring.");
        } else {
            if(saveOrUpdate.equalsIgnoreCase("S")) {
                departmentService.createDepartment(dept);
                employeeService.createEmployee(name, dept);
            } else {
                employeeService.updateDepartment(name, dept);
            }
            messageSender.sendToResQueue(name+"-"+dept);
            messageSender.sendToAckQueue(name+"-"+dept);
        }
    }
}