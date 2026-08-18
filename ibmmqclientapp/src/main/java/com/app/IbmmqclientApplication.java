package com.app;

import com.app.transaction.TransactionGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ibm.mq.jakarta.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.QueueReceiver;
import jakarta.jms.QueueSender;
import jakarta.jms.QueueSession;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

public class IbmmqclientApplication {

	// Connection parameters
    private static final String HOST = "localhost";
    private static final int PORT = 1414;
    /*private static final String QUEUE_MANAGER = "APPQM";
    private static final String CHANNEL = "APP.SVRCONN";
    private static final String QUEUE_NAME = "APP.IN.QUEUE.1";
    private static final String APP_USER = "appusr";
    private static final String APP_PASSWORD = "Pass@1234";*/
    private static final String QUEUE_MANAGER = "QM1";
    private static final String CHANNEL = "DEV.APP.SVRCONN";
    private static final String QUEUE_NAME = "DEV.QUEUE.1";
    private static final String APP_USER = "app";
    private static final String APP_PASSWORD = "Pass@4321";
    private static final int n = 1;
    
    private QueueConnectionFactory factory;
    private QueueConnection connection;
    private QueueSession session;
    
    // Initialize connection
    public void connect() throws JMSException {
        factory = new MQQueueConnectionFactory();
        ((MQQueueConnectionFactory) factory).setHostName(HOST);
        ((MQQueueConnectionFactory) factory).setPort(PORT);
        ((MQQueueConnectionFactory) factory).setQueueManager(QUEUE_MANAGER);
        ((MQQueueConnectionFactory) factory).setChannel(CHANNEL);
        ((MQQueueConnectionFactory) factory).setTransportType(WMQConstants.WMQ_CM_CLIENT);
        
        connection = factory.createQueueConnection(APP_USER, APP_PASSWORD);
        session = connection.createQueueSession(true, Session.SESSION_TRANSACTED);
        connection.start();
        
        System.out.println("Connected to IBM MQ successfully!");
    }
    
    // Send message to queue
    public void sendMessage(String messageText) throws JMSException {
        Queue queue = session.createQueue(QUEUE_NAME);
        try (QueueSender sender = session.createSender(queue)) {
            
            TextMessage message = session.createTextMessage(messageText);
            sender.send(message);
            
            System.out.println("Sent message: " + messageText);
        }
    }
    
    // Receive message from queue
    public String receiveMessage() throws JMSException {
        Queue queue = session.createQueue(QUEUE_NAME);
        String receivedText;
        try (QueueReceiver receiver = session.createReceiver(queue)) {
            Message message = receiver.receive(5000); // 5 second timeout
            receivedText = null;
            if (message instanceof TextMessage textMessage) {
                receivedText = textMessage.getText();
                System.out.println("Received message: " + receivedText);
            }
        } // 5 second timeout
        return receivedText;
    }
    
    // Close connection
    public void disconnect() throws JMSException {
        if (session != null) session.close();
        if (connection != null) connection.close();
        System.out.println("Disconnected from IBM MQ");
    }
    
    public static void main(String[] args) {
        IbmmqclientApplication client = new IbmmqclientApplication();
        TransactionGenerator transactionGenerator = new TransactionGenerator();
        try {
            client.connect();
            
            //generate message
            String message;
            for(int i = 0; i< n; i++ ) {
                message = transactionGenerator.generateMessage();
                // Send a message
                //client.sendMessage(message);
                client.sendMessage("message");
            }
            
            // Receive a message
            //client.receiveMessage();
            client.commit();
        } catch (JMSException | JsonProcessingException e) {
            e.printStackTrace();
            try {
                client.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                client.disconnect();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    public void commit() throws JMSException {
        if(session!=null)
            session.commit();
    }

    public void rollback() throws JMSException {
        if(session!=null)
            session.rollback();
    }

    

}
