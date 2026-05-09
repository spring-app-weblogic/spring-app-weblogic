package com.app.service;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.ErrorQueueDtls;
import com.app.db.repository.ErrorQueueDtlsRepository;

@Component
public class ErrorQueueSender {

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private ErrorQueueDtlsRepository errorQueueDtlsRepository;

    private final Logger logger = LoggerFactory.getLogger(ErrorQueueSender.class);

    private String hmacSha256(String data) {
         String secret = "secret";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key =
                new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(key);

            byte[] rawHmac = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }

    @Transactional(transactionManager="jtaTransactionManager", propagation=Propagation.REQUIRES_NEW)
    public void sendToFailedQueue(String message) {

        String hash = hmacSha256(message);
        long count = errorQueueDtlsRepository.countByMessageHash(hash);
        if(count == 0) {
            ErrorQueueDtls errorQueueDtls = new ErrorQueueDtls();
            errorQueueDtls.setMessage(message);
            errorQueueDtls.setMessageHash(hash);
            errorQueueDtlsRepository.save(errorQueueDtls);
            messageSender.sendToErrorQueue(message);
        } else {
            logger.info("Message {} already in error queue");
        }

        
    }


}
