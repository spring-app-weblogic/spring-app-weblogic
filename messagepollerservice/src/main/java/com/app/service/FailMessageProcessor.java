package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.FailMessage;
import com.app.db.repository.FailMessageRepository;

@Component
public class FailMessageProcessor {

    @Autowired
    private FailMessageRepository failMessageRepository;

    @Autowired
    private MessageSender messageSender;

    @Transactional(transactionManager="jtaTransactionManager", propagation=Propagation.REQUIRES_NEW)
    public void insertFailedMessage(String name, String department, String failureReason) {
        FailMessage failMessage = new FailMessage();
        failMessage.setDepartment(department);
        failMessage.setName(name);
        failMessage.setFailureReason(failureReason);
        failMessageRepository.save(failMessage);
    }

    @Transactional(transactionManager="jtaTransactionManager", propagation=Propagation.REQUIRES_NEW)
    public void sendToFailedQueue(String message) {
        messageSender.sendToErrorQueue(message);
    }

    public boolean searchFailedMessage(String name, String department) {
        int count = failMessageRepository.countByNameAndDepartment(name, department);
        return count > 0;
    }


}
