package com.app;

import java.util.Properties;

import javax.naming.NamingException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiTemplate;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.jta.WebLogicJtaTransactionManager;

@Configuration
public class JTAConfig {

    @Bean
    public JndiTemplate jndiTemplate() {
        JndiTemplate jndiTemplate = new JndiTemplate();
        Properties properties = new Properties();
        properties.put("java.naming.factory.initial", "weblogic.jndi.WLInitialContextFactory");
        jndiTemplate.setEnvironment(properties);
        return jndiTemplate;
    }

    @Bean
    public JtaTransactionManager jtaTransactionManager() throws NamingException {
        JtaTransactionManager jtaTxManager = new WebLogicJtaTransactionManager();
        jtaTxManager.setAllowCustomIsolationLevels(true);
        return jtaTxManager;
    }

}
