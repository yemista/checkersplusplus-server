package com.checkersplusplus.config;

//@Configuration
//@EnableTransactionManagement
public class HibernateConfig {
	
//	@Bean
//    public LocalSessionFactoryBean sessionFactory() {
//        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
//        sessionFactory.setDataSource(dataSource());
//        sessionFactory.setPackagesToScan("com.checkersplusplus.dao.models");
//        sessionFactory.setHibernateProperties(hibernateProperties());
//
//        return sessionFactory;
//    }
//
//    @Bean
//    public DataSource dataSource() {
//        BasicDataSource dataSource = new BasicDataSource();
//        dataSource.setDriverClassName("org.postgresql.Driver");
//        dataSource.setUrl("jdbc:postgresql://localhost:5432/checkersplusplus");
//        dataSource.setUsername("checkers");
//        dataSource.setPassword("Ch3ckers123!");
//        
//        // Connection pooling properties
//        dataSource.setInitialSize(10);
//        dataSource.setMaxIdle(5);
//        dataSource.setMaxTotal(50);
//        dataSource.setMinIdle(0);
//        return dataSource;
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager() {
//        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
//        transactionManager.setSessionFactory(sessionFactory().getObject());
//        return transactionManager;
//    }
//
//    private final Properties hibernateProperties() {
//        Properties hibernateProperties = new Properties();
//        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
//        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
//        return hibernateProperties;
//    }
}
