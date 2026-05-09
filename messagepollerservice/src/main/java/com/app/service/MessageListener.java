package com.app.service;

import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.model.TransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class MessageListener {

    @Autowired
    private MessageProcessor messageProcessor;

    @Autowired
    private FailMessageProcessor failMessageProcessor;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ErrorQueueSender errorQueueSender;

    private final static Logger logger = LoggerFactory.getLogger(MessageListener.class);
    
    @JmsListener(destination = "${app.mq.source.queue.jndi-name}", containerFactory = "jmsListenerContainerFactory")
    @Transactional(transactionManager="jtaTransactionManager", propagation=Propagation.REQUIRED)
    public void processOrder(@Payload String message) {

        String randomeString = UUID.randomUUID().toString();
        Thread.currentThread().setName(Thread.currentThread().getName() + "--" + randomeString);

        logger.info("Received data: {}", message);

        TransactionRequest transaction = null;
        try {
            
            transaction = objectMapper.readValue(message, TransactionRequest.class);
            messageProcessor.processMessage(transaction);

            logger.info("Received Transaction: {} processing successfully", transaction.getTransactionId());
        } catch (Throwable e) {
            logger.info("Throwable Exception Occured", e);
            if(Objects.isNull(transaction)) {
                errorQueueSender.sendToFailedQueue(message);
            } else {
                try {
                    failMessageProcessor.insertFailedMessage(transaction, e.getMessage());
                } catch (Throwable exp) {
                    errorQueueSender.sendToFailedQueue(message);
                }    
            }
            logger.info("Received Transaction: {} processing Failed", message);
        }
        
    }

}