package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.db.entity.TransactionSndrDtls;
import com.app.db.entity.TransactionSndrFailDtls;
import com.app.db.repository.TransactionSndrFailDtlsRepository;

@Component
public class MessageFailureHandler {

    @Autowired
    private TransactionSndrFailDtlsRepository transactionSndrFailDtlsRepository;

    @Transactional(transactionManager="jtaTransactionManager")
    public void processFailedTransaction(TransactionSndrDtls transactionSndrDtls) {
        TransactionSndrFailDtls transactionSndrFailDtls = new TransactionSndrFailDtls();
        transactionSndrFailDtls.setFailureReason(null);
        transactionSndrFailDtls.setTransactionId(transactionSndrDtls.getTransactionId());
        transactionSndrFailDtlsRepository.save(transactionSndrFailDtls);
    }

}
