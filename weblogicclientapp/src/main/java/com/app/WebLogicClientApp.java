package com.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.UserTransaction;

public class WebLogicClientApp {

    private static final String UT_JNDI     = "javax.transaction.UserTransaction";
    private static final String DS_JNDI     = "jdbc/myappDS";   // XA DataSource
    private static final String CF_JNDI     = "eis/wmq/ConnectionFactory";   // XA ConnectionFactory
    private static final String IN_QUEUE_JNDI  = "jms/mq/devqueue1";
    private static final String RES_QUEUE_JNDI  = "jms/mq/devqueue2";

    public static void main(String[] args) throws NamingException, NotSupportedException, Exception {
        WebLogicClientApp webLogicClientApp = new WebLogicClientApp();
        Properties props = webLogicClientApp.generateProperties();
        Context ctx = new InitialContext(props);
        UserTransaction userTransaction = webLogicClientApp.fetchUserTransaction(ctx);
        DataSource dataSource = webLogicClientApp.fetchDataSource(ctx);
        ConnectionFactory connectionFactory = webLogicClientApp.fetchConnectionFactory(ctx);
        Queue inQueue = webLogicClientApp.fetchQueue(ctx, IN_QUEUE_JNDI);
        Queue outQueue = webLogicClientApp.fetchQueue(ctx, RES_QUEUE_JNDI);

        userTransaction.begin();                          
        try {
            String data = webLogicClientApp.fetchFromMQ(connectionFactory, inQueue);
            if(data != null) {
                Transaction transaction = webLogicClientApp.convertToTransaction(data);
                webLogicClientApp.insertIntoDB(dataSource, transaction);
                webLogicClientApp.insertIntoMQ(connectionFactory, outQueue, data);
            }
            userTransaction.commit();                    
            System.out.println("Transaction committed — DB row and MQ message are atomic.");
        } catch (Exception e) {
            userTransaction.rollback();                   
            System.err.println("Transaction rolled back: " + e.getMessage());
            throw e;
        }
        
    }

    private Properties generateProperties() throws NamingException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        props.put(Context.PROVIDER_URL, "t3://localhost:7001");
        props.put(Context.SECURITY_PRINCIPAL, "weblogic");
        props.put(Context.SECURITY_CREDENTIALS, "PassWord@123");
        return props;
    }

    private UserTransaction  fetchUserTransaction(Context ctx) throws NamingException {
        return (UserTransaction) ctx.lookup(UT_JNDI);
    }

    private DataSource fetchDataSource(Context ctx) throws NamingException {
        return (DataSource) ctx.lookup(DS_JNDI);
    }

    private ConnectionFactory fetchConnectionFactory(Context ctx) throws NamingException {
        return (ConnectionFactory) ctx.lookup(CF_JNDI);
    }

    private Queue fetchQueue(Context ctx, String jndiName) throws NamingException {
        return (Queue) ctx.lookup(jndiName);
    }

    private void insertIntoDB(DataSource dataSource, Transaction transaction) throws SQLException {
        try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO Transaction(TRANSACTION_ID, AMOUNT, STATUS, BUSSINESS_DATE, ACCOUNT_NUMBER, version, CREATION_DATE) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    ps.setString(1, transaction.getTransactionId());
                    ps.setDouble(2, transaction.getAmount());
                    ps.setLong(3, 0l);
                    ps.setString(4, transaction.getDate());
                    ps.setString(5, transaction.getAccountNumber());
                    ps.setLong(6, 0l);
                    ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));       
                    ps.executeUpdate();
        }
    }

    private void insertIntoMQ(ConnectionFactory connectionFactory, Queue queue, String data) throws SQLException, JMSException {
        try (jakarta.jms.Connection jmsConn  = connectionFactory.createConnection();
                 Session     session   = jmsConn.createSession();   // XA session via WL
                 MessageProducer prod  = session.createProducer(queue)) {

                TextMessage msg = session.createTextMessage(data);
                prod.send(msg);
        }
    }

    private String fetchFromMQ(ConnectionFactory connectionFactory, Queue queue) throws SQLException, JMSException {
        try (jakarta.jms.Connection jmsConn  = connectionFactory.createConnection();
                 Session     session   = jmsConn.createSession();   // XA session via WL
                 MessageConsumer consumer  = session.createConsumer(queue)) {
                 
                    Message message = consumer.receive(1000);
                    if(message == null) {
                        return null;
                    }
                    return message.getBody(String.class);
        }
    }

    private Transaction convertToTransaction(String input) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(input, Transaction.class);
    }
    

}
