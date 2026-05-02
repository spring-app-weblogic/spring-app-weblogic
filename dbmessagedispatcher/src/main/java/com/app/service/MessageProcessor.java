package com.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessor {

    @Autowired
    private MessagePicker messagePicker;

    Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    @Scheduled(fixedDelay = 10000)
    public void pollMessages() {
        try {
            log.info("Inside MessageProcessor pollMessages() method");
            messagePicker.handleMessages();
        } catch (Exception e) {
            log.error("Exception occured in MessageProcessor pollMessages() method", e);
        }
    }

}
