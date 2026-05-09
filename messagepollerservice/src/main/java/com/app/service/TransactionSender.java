package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.TransactionSndrDtls;
import com.app.db.repository.TransactionSndrDtlsRepository;
import com.app.model.TransactionRequest;

@Component
public class TransactionSender {

    @Autowired
    private TransactionSndrDtlsRepository transactionSndrDtlsRepository;

    @Transactional(transactionManager="jtaTransactionManager")
    public void createTransactionSender(TransactionRequest transaction) {
            TransactionSndrDtls transactionSndrDtls = new TransactionSndrDtls();
            transactionSndrDtls.setTransactionId(transaction.getTransactionId());
            transactionSndrDtlsRepository.save(transactionSndrDtls);    
    }

}
