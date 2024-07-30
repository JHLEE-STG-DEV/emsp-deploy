package com.chargev.emsp.config;

import java.util.HashMap;
import java.util.Map;
 
import javax.sql.DataSource;
 
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
 
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "poiEntityManagerFactory", transactionManagerRef = "poiTransactionManager", basePackages = { "com.chargev.emsp.repository.poi" })
public class PoiDataSourceConfiguration {
 
	@Bean
	@ConfigurationProperties(prefix="spring.poidatasource")
	public DataSource poiDataSource() {
		return DataSourceBuilder.create().build();
	}
 
	@Bean
    public LocalContainerEntityManagerFactoryBean poiEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        em.setDataSource(poiDataSource());
		em.setPackagesToScan("com.chargev.emsp.entity.poi");
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        em.setJpaPropertyMap(properties);
        em.setJpaVendorAdapter(vendorAdapter);


        return em;
    }

    @Bean
    public PlatformTransactionManager poiTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(poiEntityManagerFactory().getObject());
        return transactionManager;
    }
}