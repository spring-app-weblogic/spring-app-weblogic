package com.app.service;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.Employee;
import com.app.db.repository.EmployeeRepository;

import jakarta.annotation.PreDestroy;

@Component
public class MessagePicker {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private EmployeeProcessor employeeProcessor;

    Logger log = LoggerFactory.getLogger(MessagePicker.class);

    public static final Semaphore THREAD_COUNT = new Semaphore(5);

    @Transactional(transactionManager="jtaTransactionManager", propagation=Propagation.REQUIRES_NEW)
    public void handleMessages() throws InterruptedException {
        List<Employee> employees = employeeRepository.fetchAndLockEmployees(10);
        for(Employee employee: employees) {
            THREAD_COUNT.acquire();
            threadPoolTaskExecutor.execute(() -> {
                try {
                    employeeProcessor.process(employee);
                } finally {
                    THREAD_COUNT.release();
                }
            });
        }
        while(threadPoolTaskExecutor.getActiveCount() > 0) {
            TimeUnit.SECONDS.sleep(10);
        }
        List<Long> ids = employees.stream().map(emp -> emp.getId()).toList();
        
        if(!ids.isEmpty())
            employeeRepository.updateStatusForIds(1, ids);
    }

    @PreDestroy
    public void shutdown() {
        threadPoolTaskExecutor.initiateShutdown();
        threadPoolTaskExecutor.shutdown();
        log.info("Stopping Executor...");
    }

}