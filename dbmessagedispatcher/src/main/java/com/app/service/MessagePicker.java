package com.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.TransactionSndrDtls;
import com.app.db.repository.TransactionSndrDtlsRepository;
import com.app.exception.TransactionException;

import jakarta.annotation.PreDestroy;

@Component
public class MessagePicker {

    @Autowired
    private TransactionSndrDtlsRepository transactionSndrDtlsRepository;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private TransactionProcessor transactionProcessor;

    @Autowired
    private MessageFailureHandler messageFailureHandler;

    Logger log = LoggerFactory.getLogger(MessagePicker.class);

    public static final Semaphore THREAD_COUNT = new Semaphore(5);

    @Transactional(transactionManager="jtaTransactionManager", propagation=Propagation.REQUIRES_NEW)
    public void handleMessages() throws InterruptedException {
        List<TransactionSndrDtls> transactionDtlsList = transactionSndrDtlsRepository.fetchAndLockTransactions(10);
        List<CompletableFuture<TransactionSndrDtls>> futures = new ArrayList<>();
        log.info("Entering the batch details");
        for(TransactionSndrDtls transactionSndrDtls: transactionDtlsList) {
            THREAD_COUNT.acquire();
            CompletableFuture<TransactionSndrDtls> future = transactionProcessor.process(transactionSndrDtls, THREAD_COUNT);
            futures.add(future);
        }
        log.info("All picked transactions are initiated..");
        while(threadPoolTaskExecutor.getActiveCount() > 0) {
            TimeUnit.SECONDS.sleep(10);
        }

        for (CompletableFuture<TransactionSndrDtls> future : futures) {
            future.thenAccept(transactionSndrDtls -> {
                log.info("Deleting the transaction");
                transactionSndrDtlsRepository.delete(transactionSndrDtls);
            }).exceptionally(ex -> {
                log.error("Exception occured in future", ex);
                Throwable cause = ex.getCause();
                if(cause instanceof TransactionException exception) {
                    transactionSndrDtlsRepository.delete(exception.getTransactionSndrDtls());
                    messageFailureHandler.processFailedTransaction(exception.getTransactionSndrDtls());
                }
                return null;
            });
        }
        log.info("End of the method");
    }

    @PreDestroy
    public void shutdown() {
        threadPoolTaskExecutor.initiateShutdown();
        threadPoolTaskExecutor.shutdown();
        log.info("Stopping Executor...");
    }

}