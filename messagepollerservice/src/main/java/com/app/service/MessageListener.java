package com.app.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class MessageListener {

    @Autowired
    private MessageProcessor messageProcessor;

    @Autowired
    private FailMessageProcessor failMessageProcessor;
    
    @JmsListener(destination = "${app.mq.source.queue.jndi-name}", containerFactory = "jmsListenerContainerFactory")
    @Transactional(transactionManager="jtaTransactionManager")
    public void processOrder(@Payload String message) {

        String randomeString = UUID.randomUUID().toString();
        Thread.currentThread().setName(Thread.currentThread().getName() + "--" + randomeString);

        System.out.println("Received data: " + message);

        String[] data = message.split("-");
        try {
            switch (data.length) {
                case 3 -> messageProcessor.processMessage(data[0], data[1], data[2]);
                case 2 -> messageProcessor.processMessage(data[0], data[1], "S");
                default -> throw new RuntimeException("Message Format is InCorrect");
            }
        } catch (Throwable e) {
            System.out.println("Exception catched....");
            switch (data.length) {
                case 3, 2 -> failMessageProcessor.insertFailedMessage(data[0], data[1], e.getMessage());
                default -> failMessageProcessor.sendToFailedQueue(message);
            }
        }
        System.out.println("Received data: " + message + " processing successfully");
    }
    
}
