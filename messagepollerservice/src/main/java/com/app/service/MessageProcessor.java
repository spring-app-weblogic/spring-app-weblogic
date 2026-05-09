package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.model.TransactionRequest;

@Service
public class MessageProcessor {

    @Autowired
    private TransactionProcessor transactionProcessor;

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private TransactionSender transactionSender;

    @Autowired
    private FailMessageProcessor failMessageProcessor;

    @Transactional(transactionManager="jtaTransactionManager")
    public void processMessage(TransactionRequest transaction) {
        if(failMessageProcessor.searchFailedMessage(transaction.getTransactionId())) {
            System.out.println("Message already present in failed message table, hence ignoring.");
        } else {
            transactionProcessor.createTransaction(transaction);
            transactionSender.createTransactionSender(transaction);
            
            messageSender.sendToAckQueue(transaction.getTransactionId());
        }
    }
}