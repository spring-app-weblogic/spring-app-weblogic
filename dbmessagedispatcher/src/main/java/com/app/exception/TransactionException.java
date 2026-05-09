package com.app.exception;

import com.app.db.entity.TransactionSndrDtls;

public class TransactionException extends RuntimeException {

    private TransactionSndrDtls transactionSndrDtls;

    public TransactionException(TransactionSndrDtls transactionSndrDtls) {
        this.transactionSndrDtls = transactionSndrDtls;
    }

    public TransactionSndrDtls getTransactionSndrDtls() {
        return transactionSndrDtls;
    }

}
