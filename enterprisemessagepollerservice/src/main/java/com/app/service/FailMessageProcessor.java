package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.TransactionFailDtls;
import com.app.db.repository.TransactionFailDtlsRepository;
import com.app.model.TransactionRequest;

@Component
public class FailMessageProcessor {

    @Autowired
    private TransactionFailDtlsRepository transactionFailDtlsRepository;

    @Transactional(transactionManager="jtaTransactionManager")
    //@Transactional(transactionManager="jtaTransactionManager", propagation=Propagation.REQUIRES_NEW)
    public void insertFailedMessage(TransactionRequest transactionRequest, String failureReason) {
        TransactionFailDtls transactionFailDtlsMessage = new TransactionFailDtls();
        transactionFailDtlsMessage.setAccountNumber(transactionRequest.getAccountNumber());
        transactionFailDtlsMessage.setAmount(transactionRequest.getAmount());
        transactionFailDtlsMessage.setDate(transactionRequest.getDate());
        transactionFailDtlsMessage.setFailureReason(failureReason);
        transactionFailDtlsMessage.setStatus(0);
        transactionFailDtlsMessage.setTransactionId(transactionRequest.getTransactionId());
        transactionFailDtlsRepository.save(transactionFailDtlsMessage);
    }

    public boolean searchFailedMessage(String transactionId) {
        long count = transactionFailDtlsRepository.countByTransactionId(transactionId);
        return count > 0;
    }


}
