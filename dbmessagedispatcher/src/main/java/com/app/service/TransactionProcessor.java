package com.app.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.TransactionDtls;
import com.app.db.entity.TransactionSndrDtls;
import com.app.db.repository.TransactionDtlsRepository;
import com.app.exception.TransactionException;
import com.app.model.TransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TransactionProcessor {

    private Logger log = LoggerFactory.getLogger(TransactionProcessor.class);

    @Autowired
    private TransactionDtlsRepository transactionDtlsRepository;

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional(transactionManager="jtaTransactionManager", propagation=Propagation.REQUIRES_NEW)
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<TransactionSndrDtls> process(TransactionSndrDtls transactionSndrDtls, Semaphore threadCount) {
        TransactionDtls transactionDtls = null;
        try{
            transactionDtls = transactionDtlsRepository.findByTransactionId(transactionSndrDtls.getTransactionId());
            if(transactionDtls.getStatus() == 0) {
                transactionDtls.setStatus(1);
                transactionDtlsRepository.save(transactionDtls);
                TransactionRequest transactionRequest = generateRequest(transactionDtls);

                String result = objectMapper.writeValueAsString(transactionRequest);

                messageSender.sendToResQueue(result);
            } else {
                log.info("Transaction with id {} is already processed", transactionSndrDtls.getTransactionId());
            }
        } catch(Exception exp) {
            log.error("Exception Occured while processing the transaction", exp);
            throw new TransactionException(transactionSndrDtls);
        } finally {
            threadCount.release();
        }
        return CompletableFuture.completedFuture(transactionSndrDtls);
         
    }

    private TransactionRequest generateRequest(TransactionDtls transactionDtls) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setTransactionId(transactionDtls.getTransactionId());
        transactionRequest.setAmount(transactionDtls.getAmount());
        transactionRequest.setAccountNumber(transactionDtls.getAccountNumber());
        transactionRequest.setTimestamp(transactionDtls.getDate());
        return transactionRequest;
    }

}
