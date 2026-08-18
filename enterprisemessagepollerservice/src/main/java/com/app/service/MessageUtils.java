package com.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import weblogic.transaction.TransactionHelper;

public class MessageUtils {

    private final static Logger logger = LoggerFactory.getLogger(MessageUtils.class);

    public static void printTXID() {
        try {
            Transaction tx = TransactionHelper.getTransactionHelper()
                                            .getTransactionManager()
                                            .getTransaction();
            if (tx != null) {
                // Option A: Full String representation (includes state, XID, etc.)
                String txString = tx.toString();
                
                logger.info("Current WebLogic Tx in processMessage is : " + txString);
            } else {
                logger.info("No active transaction found! in processMessage");
            }
        } catch(SystemException se) {
            logger.error("Error Occured while fetching JTA Transaction ID");
        }
        
    }

}
