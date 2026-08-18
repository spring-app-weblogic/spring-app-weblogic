package com.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.exception.TransientMessageProcessingException;

import jakarta.jms.Message;

@Component
public class ErrorQueueSender {

    @Autowired
    private MessageSender messageSender;

    private final Logger logger = LoggerFactory.getLogger(ErrorQueueSender.class);

    @Transactional(transactionManager="jtaTransactionManager")
    public void sendToFailedQueue(Message queueMessage) {
        try {
            messageSender.sendToErrorQueue(queueMessage);
        } catch (JmsException e) {
            throw new TransientMessageProcessingException("MQ_ERROR", "Exception Occured in Error Queue", e);
        }
    }

    @Transactional(transactionManager="jtaTransactionManager")
    public void sendToFaultQueue(Message queueMessage) {
        try {
            messageSender.sendToFaultQueue(queueMessage);
        } catch (JmsException e) {
            throw new TransientMessageProcessingException("MQ_FAULT", "Exception Occured in Fault Queue", e);
        }
    }
}
