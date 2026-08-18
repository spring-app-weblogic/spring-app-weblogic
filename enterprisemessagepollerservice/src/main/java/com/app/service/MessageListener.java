package com.app.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.exception.MessageProcessingException;
import com.app.exception.TransientMessageProcessingException;
import com.app.model.TransactionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;


@Component
public class MessageListener {

    private final MessageProcessor messageProcessor;

    private final FailMessageProcessor failMessageProcessor;

    private final ObjectMapper objectMapper;

    private final ErrorQueueSender errorQueueSender;

    private final static Logger logger = LoggerFactory.getLogger(MessageListener.class);

    MessageListener(ObjectMapper objectMapper, MessageProcessor messageProcessor, ErrorQueueSender errorQueueSender,
                        FailMessageProcessor failMessageProcessor) {
        this.objectMapper = objectMapper;
        this.messageProcessor = messageProcessor;
        this.failMessageProcessor = failMessageProcessor;
        this.errorQueueSender = errorQueueSender;
    }
    
    @JmsListener(destination = "${app.mq.source.queue.jndi-name}", containerFactory = "jmsListenerContainerFactory")
    @Transactional(transactionManager="jtaTransactionManager", propagation=Propagation.REQUIRED)
    public void processOrder(Message queueMessage) {

        TransactionRequest transaction = null;
        boolean isRedelivered = false;
        try {
            MessageUtils.printTXID();
        
            changeThreadName();

            if(queueMessage instanceof TextMessage inpuTextMessage) {
                try {
                    String message = inpuTextMessage.getBody(String.class);

                    isRedelivered = inpuTextMessage.getJMSRedelivered();

                    int deliveryCount = 1;
                    if (inpuTextMessage.propertyExists("JMSXDeliveryCount")) {
                        deliveryCount = inpuTextMessage.getIntProperty("JMSXDeliveryCount");
                    }

                    logger.info("Received data: {}, isRedelivered {}", message, isRedelivered);

                    if(deliveryCount > 5) {
                        errorQueueSender.sendToFailedQueue(queueMessage);
                        return;
                    } 
                    transaction = objectMapper.readValue(message, TransactionRequest.class);
                } catch (JMSException jmse ) {
                    errorQueueSender.sendToFaultQueue(queueMessage);
                } catch (JsonProcessingException jmse ) {
                    errorQueueSender.sendToFaultQueue(queueMessage);
                } 
                messageProcessor.processMessage(transaction, isRedelivered);
            } else {
                errorQueueSender.sendToFaultQueue(queueMessage);
            }
        } catch (MessageProcessingException mpe) {
            failMessageProcessor.insertFailedMessage(transaction, mpe.getMessage());        
        } catch (TransactionException mpe) {
            failMessageProcessor.insertFailedMessage(transaction, mpe.getMessage());        
        } catch (TransientMessageProcessingException tpe) {
            logger.error("TransientMessageProcessingException Occured", tpe);
            throw tpe;
        } catch (RuntimeException re) {
            logger.error("RuntimeException Occured", re);
            throw re;
        }
    }

    private void changeThreadName() {
        String randomeString = UUID.randomUUID().toString();
        String existingThreadName = Thread.currentThread().getName();
        if(existingThreadName.contains("--")) {
            String newThreadName = existingThreadName.replaceFirst("(?<=--)[0-9a-fA-F-]+$", randomeString);
            Thread.currentThread().setName(newThreadName);
        } else {
            Thread.currentThread().setName(Thread.currentThread().getName() + "--" + randomeString);
        }
    }

}