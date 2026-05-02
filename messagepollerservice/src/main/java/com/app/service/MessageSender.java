package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class MessageSender {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${app.mq.dest.res.queue.jndi-name:jms/mq/devqueue2}")
    private String destResQueueName;

    @Value("${app.mq.dest.ack.queue.jndi-name:jms/mq/devqueue3}")
    private String destAckQueueName;

    @Value("${app.mq.dest.error.queue.jndi-name:jms/mq/deverrorqueue}")
    private String destErrorQueueName;

    /**
     * Send message to specific queue
     */
    @Transactional(transactionManager="jtaTransactionManager")
    public void sendToResQueue(String message) {
        jmsTemplate.convertAndSend(destResQueueName, message);
        System.out.println("Message Successfully into Queue");
    }

    @Transactional(transactionManager="jtaTransactionManager")
    public void sendToAckQueue(String message) {
        jmsTemplate.convertAndSend(destAckQueueName, message);
        System.out.println("Message Successfully into Queue");
    }

    @Transactional(transactionManager="jtaTransactionManager")
    public void sendToErrorQueue(String message) {
        jmsTemplate.convertAndSend(destErrorQueueName, message);
        System.out.println("Message Successfully into Error Queue");
    }

}
