package com.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.Employee;

@Component
public class EmployeeProcessor {

    private Logger log = LoggerFactory.getLogger(EmployeeProcessor.class);

    @Transactional(transactionManager="jtaTransactionManager", propagation=Propagation.REQUIRES_NEW)
    public void process(Employee employee) {
        log.info("Processed Employee Record");
    }

}
