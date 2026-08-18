package com.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jms.JmsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.exception.MessageProcessingException;
import com.app.exception.ValidationException;
import com.app.model.TransactionRequest;

@Service
public class MessageProcessor {

    private final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    private final TransactionProcessor transactionProcessor;

    private final MessageSender messageSender;

    private final TransactionSender transactionSender;

    private final FailMessageProcessor failMessageProcessor;

    MessageProcessor(TransactionProcessor transactionProcessor, MessageSender messageSender,
            TransactionSender transactionSender, FailMessageProcessor failMessageProcessor) {
        this.transactionProcessor = transactionProcessor;
        this.messageSender = messageSender;
        this.transactionSender = transactionSender;
        this.failMessageProcessor = failMessageProcessor;        
    }

    @Transactional(transactionManager="jtaTransactionManager", propagation=Propagation.REQUIRES_NEW)
    public void processMessage(TransactionRequest transaction, boolean isRedelivered) {
        try {
            if (transaction == null || transaction.getTransactionId() == null) {
                logger.error("Received null transaction or transaction ID.");
                throw new ValidationException("MSG_TRN_NULL", "Transaction or Transaction ID cannot be null");
            }
            MessageUtils.printTXID(); 
            
            String txId = transaction.getTransactionId();

            if (isRedelivered) {
                if (transactionProcessor.isTransactionAlreadyProcessed(txId)) {
                    logger.info("Transaction {} already processed successfully.", txId);
                    return;
                }
            }

            transactionProcessor.createTransaction(transaction);
            transactionSender.createTransactionSender(transaction, "S");
            messageSender.sendToAckQueue(txId);

        } catch (ValidationException ve) {
            logger.error("Validation failed for transaction: {}", transaction.getTransactionId(), ve);
            throw new MessageProcessingException("VAL_001", "Transaction validation failed: ", ve);
        } catch (DataAccessException dae) {
            logger.error("Unexpected database error occured while processing transaction: {}", transaction.getTransactionId());
            throw new MessageProcessingException("DB_ERROR", "DB JPA Error", dae);
        } catch (JmsException jms) {
            logger.error("Unexpected MQ error occured while processing transaction: {}", transaction.getTransactionId());
            throw new MessageProcessingException("MQ_ERROR", "MQ Error", jms);
        } catch (RuntimeException re) {
            logger.error("Unexpected Application error occured while processing transaction: {}", transaction.getTransactionId());
            throw new MessageProcessingException("APP_ERROR", "Application Error", re);
        } 
    }

    
}