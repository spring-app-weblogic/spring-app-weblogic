package com.app.mq;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.backoff.FixedBackOff;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.jms.ConnectionFactory;

@Configuration
@EnableJms
@EnableTransactionManagement
@EnableRetry
public class MqConfig {

    @Value("${app.mq.jndi-name:eis/wmq/ConnectionFactory}")
    private String mqConJndiName;

    @Value("${app.mq.executor-jndi:java:comp/DefaultManagedExecutorService}")
    private String managedExecutorJndiName;

    private final static Logger logger = LoggerFactory.getLogger(MqConfig.class);

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
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory jmsConnectionFactory, JtaTransactionManager jtaTransactionManager,
                                    JndiTemplate jndiTemplate) throws NamingException {
        DefaultJmsListenerContainerFactory factory = 
            new DefaultJmsListenerContainerFactory();
        
        factory.setConnectionFactory(jmsConnectionFactory);
        factory.setTransactionManager(jtaTransactionManager);
        factory.setSessionTransacted(false);

        ManagedExecutorService weblogicExecutor = (ManagedExecutorService) jndiTemplate.lookup(managedExecutorJndiName);
        factory.setTaskExecutor(new ConcurrentTaskExecutor(weblogicExecutor));
        
        // Retry every 10 seconds indefinitely during an MQ outage
        factory.setBackOff(new FixedBackOff(10000L, FixedBackOff.UNLIMITED_ATTEMPTS));
        

        factory.setConcurrency("3-10");
        //factory.setCacheLevel(DefaultMessageListenerContainer.CACHE_AUTO);
        factory.setCacheLevel(DefaultMessageListenerContainer.CACHE_NONE);
        factory.setReceiveTimeout(5000L);
        factory.setErrorHandler(t -> {
            logger.error("Critical error in JMS listener container. Transaction may be rolled back.", t);
        });
        factory.setDestinationResolver(new JndiDestinationResolver());
        return factory;
    }

}

