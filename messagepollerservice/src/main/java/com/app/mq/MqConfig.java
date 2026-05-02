package com.app.mq;

import javax.naming.NamingException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.JndiDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import jakarta.jms.ConnectionFactory;

@Configuration
@EnableJms
@EnableTransactionManagement
public class MqConfig {

    @Value("${app.mq.jndi-name:eis/wmq/ConnectionFactory}")
    private String mqConJndiName;

    @Bean
    public ConnectionFactory jmsConnectionFactory(JndiTemplate jndiTemplate) throws NamingException {
        JndiObjectFactoryBean jndiFactory = new JndiObjectFactoryBean();
        jndiFactory.setJndiTemplate(jndiTemplate);
        jndiFactory.setJndiName(mqConJndiName);
        jndiFactory.setProxyInterface(ConnectionFactory.class);
        jndiFactory.setLookupOnStartup(true);
        jndiFactory.afterPropertiesSet();
        return (ConnectionFactory) jndiFactory.getObject();
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory jmsConnectionFactory) throws NamingException {
        JmsTemplate template = new JmsTemplate(jmsConnectionFactory);
        // Enable transacted sessions - will participate in JTA transaction
        template.setSessionTransacted(false);
        // Let container handle acknowledgment (for XA)
        template.setSessionAcknowledgeMode(jakarta.jms.Session.AUTO_ACKNOWLEDGE);
        template.setDestinationResolver(new JndiDestinationResolver());
        return template;
    }
    
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory jmsConnectionFactory, JtaTransactionManager jtaTransactionManager) 
            throws NamingException {
        DefaultJmsListenerContainerFactory factory = 
            new DefaultJmsListenerContainerFactory();
        
        factory.setConnectionFactory(jmsConnectionFactory);
        factory.setTransactionManager(jtaTransactionManager);
        factory.setSessionTransacted(false);
        factory.setConcurrency("3-10");
        factory.setCacheLevel(DefaultMessageListenerContainer.CACHE_AUTO);
        factory.setErrorHandler(t -> {
            System.err.println("Error in JMS listener: " + t.getMessage());
            t.printStackTrace();
        });
        factory.setDestinationResolver(new JndiDestinationResolver());
        return factory;
    }

}

