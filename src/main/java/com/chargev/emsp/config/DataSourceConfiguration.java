package com.chargev.emsp.config;
 
import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.RequiredArgsConstructor;

 
@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = { "com.chargev.emsp.repository.authentication", "com.chargev.emsp.repository.cert", "com.chargev.emsp.repository.contract", "com.chargev.emsp.repository.key", "com.chargev.emsp.repository.log", "com.chargev.emsp.repository.oem", "com.chargev.emsp.repository.ocpi" })
public class DataSourceConfiguration {
	@Bean
	@Primary
	@ConfigurationProperties(prefix="spring.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}
 
 
	@Bean
	@Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        em.setDataSource(dataSource());
		em.setPackagesToScan("com.chargev.emsp.entity.authenticationentity", "com.chargev.emsp.entity.cert", "com.chargev.emsp.entity.contract", "com.chargev.emsp.entity.keyentity", "com.chargev.emsp.entity.log", "com.chargev.emsp.entity.oem", "com.chargev.emsp.entity.ocpi");
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        em.setJpaPropertyMap(properties);
        em.setJpaVendorAdapter(vendorAdapter);

		return em;
    }

    @Bean
	@Primary
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
}