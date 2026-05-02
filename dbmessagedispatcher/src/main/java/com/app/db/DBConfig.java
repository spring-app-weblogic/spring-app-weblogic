package com.app.db;

import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.app.db.repository",
    transactionManagerRef = "jtaTransactionManager"
)
public class DBConfig {

    @Value("${app.datasource.jndi-name:jdbc/myappDS}")
    private String dataSourceJndiName;

    @Bean
    public DataSource dataSource(JndiTemplate jndiTemplate) throws NamingException {
        JndiObjectFactoryBean jndiFactory = new JndiObjectFactoryBean();
        jndiFactory.setJndiTemplate(jndiTemplate);
        jndiFactory.setJndiName(dataSourceJndiName);
        jndiFactory.setProxyInterface(DataSource.class);
        jndiFactory.setLookupOnStartup(true);
        jndiFactory.afterPropertiesSet();
        return (DataSource) jndiFactory.getObject();
    }

    @Bean
    @DependsOn({"dataSource"})
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em =
                new LocalContainerEntityManagerFactoryBean();

        em.setJtaDataSource(dataSource);
        em.setPackagesToScan("com.app.db.entity");
        em.setJpaVendorAdapter(jpaVendorAdapter());

        // Tell Hibernate to use JTA – required when an external TM manages txns
        em.setJpaProperties(hibernateProperties());
        
        // Persistence unit name must match META-INF/persistence.xml (optional
        // when using Spring's classpath scanning, but good practice to set it)
        em.setPersistenceUnitName("myAppPU");

        return em;
    }

    private JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(false);
        adapter.setGenerateDdl(false);  
        return adapter;
    }

    private Properties hibernateProperties() {
        Properties props = new Properties();

        // --- JTA integration -----------------------------------------------
        props.setProperty(
                "hibernate.transaction.jta.platform",
                "org.hibernate.engine.transaction.jta.platform.internal.WeblogicJtaPlatform");

        props.setProperty(
                "hibernate.transaction.coordinator_class", "jta");

        // --- Connection handling --------------------------------------------
        props.setProperty(
                "hibernate.connection.handling_mode",
                "DELAYED_ACQUISITION_AND_RELEASE_AFTER_TRANSACTION");

        // --- Second-level cache (disabled by default – enable as needed) ---
        props.setProperty("hibernate.cache.use_second_level_cache", "false");
        props.setProperty("hibernate.cache.use_query_cache", "false");
        props.setProperty("hibernate.dialect","org.hibernate.dialect.OracleDialect");

        // --- DDL --------------------------------------------------------------
        // Use "none" in production; use "validate" to catch schema mismatches.
        props.setProperty("hibernate.hbm2ddl.auto", "none");

        // --- Logging (disable SQL in production) ----------------------------
        props.setProperty("hibernate.show_sql", "true");
        props.setProperty("hibernate.format_sql", "true");

        return props;
    }
}

