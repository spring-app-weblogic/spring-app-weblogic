package com.app.model;

public class TransactionRequest {

    private String transactionId;

    private double amount;

    private String status;

    private String date;

    private String accountNumber;

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getDate() {
        return date;
    }
    public void setTimestamp(String date) {
        this.date = date;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public TransactionRequest(String transactionId, double amount, String status, String date,
            String accountNumber) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.status = status;
        this.date = date;
        this.accountNumber = accountNumber;
    }

}
