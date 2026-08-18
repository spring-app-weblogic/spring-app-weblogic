package com.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.exception.ValidationException;

import jakarta.jms.Message;

@Service
@Retryable(retryFor = {JmsException.class}, maxAttempts = 3, backoff = @Backoff(delay = 5000))
public class MessageSender {

    private final JmsTemplate jmsTemplate;

    @Value("${app.mq.dest.ack.queue.jndi-name:jms/mq/devqueue3}")
    private String destAckQueueName;

    @Value("${app.mq.dest.error.queue.jndi-name:jms/mq/deverrorqueue}")
    private String destErrorQueueName;

    @Value("${app.mq.dest.fault.queue.jndi-name:jms/mq/devfaultqueue}")
    private String destFaultQueueName;

    private final static Logger logger = LoggerFactory.getLogger(MessageSender.class);

    MessageSender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Transactional(transactionManager="jtaTransactionManager")
    public void sendToAckQueue(String message) throws ValidationException {
        jmsTemplate.convertAndSend(destAckQueueName, message);
        if(message.equals("b0b74656-a1a6-4fcd-85d0-c3161655d8ed")) {
            throw new ValidationException("0001", "ID Not Valid");
        }
        logger.info("Message Successfully into Queue");
    }

    @Transactional(transactionManager="jtaTransactionManager")
    public void sendToErrorQueue(Message message) {
        jmsTemplate.convertAndSend(destErrorQueueName, message);
        logger.info("Message Successfully into Error Queue");
    }

    @Transactional(transactionManager="jtaTransactionManager")
    public void sendToFaultQueue(Message message) {
        jmsTemplate.convertAndSend(destFaultQueueName, message);
        logger.info("Message Successfully into Fault Queue");
    }

}
