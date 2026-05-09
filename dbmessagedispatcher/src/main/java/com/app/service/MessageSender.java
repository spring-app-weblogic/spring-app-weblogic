package com.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger logger = LoggerFactory.getLogger(MessageSender.class);

    /**
     * Send message to specific queue
     */
    @Transactional(transactionManager="jtaTransactionManager")
    public void sendToResQueue(String message) {
        jmsTemplate.convertAndSend(destResQueueName, message);
        logger.info("Message Successfully into Queue");
    }

}

