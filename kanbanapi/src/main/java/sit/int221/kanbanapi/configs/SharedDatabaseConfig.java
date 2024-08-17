package sit.int221.kanbanapi.configs;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:application.properties")
@EnableJpaRepositories(
        basePackages = "sit.int221.kanbanapi.databases.userdb.repositories",
        entityManagerFactoryRef = "secondaryEntityManagerFactory",
        transactionManagerRef = "secondaryTransactionManager"
)
public class SharedDatabaseConfig {
    @Autowired
    Environment env;

    @Bean(name = "secondaryDataSource")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(env.getProperty("spring.datasource.driver-class-name"))
                .url(env.getProperty("spring.second-datasource.url"))
                .username(env.getProperty("spring.second-datasource.username"))
                .password(env.getProperty("spring.second-datasource.password"))
                .build();
    }

    @Bean(name = "secondaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(secondaryDataSource())
                .packages("sit.int221.kanbanapi.databases.userdb.entities")
                .persistenceUnit("userdb")
                .build();
    }

    @Bean(name = "secondaryTransactionManager")
    public PlatformTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManagerFactory") EntityManagerFactory secondaryEntityManagerFactory) {
        return new JpaTransactionManager(secondaryEntityManagerFactory);
    }
}
