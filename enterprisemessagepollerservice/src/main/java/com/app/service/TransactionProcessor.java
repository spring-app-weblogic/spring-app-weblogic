package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.TransactionDtls;
import com.app.db.repository.TransactionDtlsRepository;
import com.app.exception.ValidationException;
import com.app.model.TransactionRequest;

@Component
public class TransactionProcessor {

    @Autowired
    private TransactionDtlsRepository transactionDtlsRepository;

    @Transactional(transactionManager="jtaTransactionManager")
    public void createTransaction(TransactionRequest transaction) throws ValidationException {
        TransactionDtls transactionDtls = new TransactionDtls();
        transactionDtls.setAccountNumber(transaction.getAccountNumber());
        transactionDtls.setAmount(transaction.getAmount());
        transactionDtls.setDate(transaction.getDate());
        transactionDtls.setStatus(0);
        transactionDtls.setTransactionId(transaction.getTransactionId());
        transactionDtlsRepository.save(transactionDtls);    
    }

    @Transactional(transactionManager="jtaTransactionManager")
    public boolean isTransactionAlreadyProcessed(String transactionId) throws ValidationException {
        return transactionDtlsRepository.countByTransactionId(transactionId) > 0;  
    }

}
