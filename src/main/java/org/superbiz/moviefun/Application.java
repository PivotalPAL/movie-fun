package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials(@Value("${VCAP_SERVICES}") String vcapServices) {
        return new DatabaseServiceCredentials(vcapServices);
    }

    @Bean(name = "albumsDataSource")
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(serviceCredentials.jdbcUrl("albums-mysql"));
        return dataSource;
    }

    @Bean(name = "moviesDataSource")
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(serviceCredentials.jdbcUrl("movies-mysql"));
        return dataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernate = new HibernateJpaVendorAdapter();

        hibernate.setDatabase(Database.MYSQL);
        hibernate.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernate.setGenerateDdl(true);
        return hibernate;
    }

    @Bean(name = "albumsEntityManager")
    public LocalContainerEntityManagerFactoryBean
    albumsEntity(@Qualifier("albumsDataSource") DataSource dataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter ) {

        LocalContainerEntityManagerFactoryBean entityBean = new LocalContainerEntityManagerFactoryBean();

        entityBean.setDataSource(dataSource);
        entityBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);

        entityBean.setPackagesToScan("org.superbiz.moviefun.albums");
        entityBean.setPersistenceUnitName("persistenceAlbums");

        return entityBean;

    }

    @Bean(name = "moviesEntityManager")
    public LocalContainerEntityManagerFactoryBean
    moviesEntity(@Qualifier("moviesDataSource") DataSource dataSource, HibernateJpaVendorAdapter hibernateJpaVendorAdapter ) {

        LocalContainerEntityManagerFactoryBean entityBean = new LocalContainerEntityManagerFactoryBean();

        entityBean.setDataSource(dataSource);
        entityBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);

        entityBean.setPackagesToScan("org.superbiz.moviefun.movies");
        entityBean.setPersistenceUnitName("persistenceMovies");

        return entityBean;
    }

    @Bean(name = "albumsPlatformTransactionManager")
    public PlatformTransactionManager AlbumsplatformTransactionManager (@Qualifier("albumsEntityManager") EntityManagerFactory entityManagerFactory)
    {
        JpaTransactionManager jpaTransactionManager =  new JpaTransactionManager(entityManagerFactory);
        return jpaTransactionManager;
    }

    @Bean(name = "moviesPlatformTransactionManager")
    public PlatformTransactionManager moviesplatformTransactionManager (@Qualifier("moviesEntityManager")EntityManagerFactory entityManagerFactory)
    {
        JpaTransactionManager jpaTransactionManager =  new JpaTransactionManager(entityManagerFactory);
        return jpaTransactionManager;
    }

    @Bean(name="albumsTransactionOperations")
    public TransactionOperations AlbumstransactionOperations
            (@Qualifier("albumsPlatformTransactionManager")PlatformTransactionManager platformTransactionManager) {

        return new TransactionTemplate(platformTransactionManager);

    }

    @Bean(name="moviesTransactionOperations")
    public TransactionOperations MoviestransactionOperations
            (@Qualifier("moviesPlatformTransactionManager")PlatformTransactionManager platformTransactionManager) {

        return new TransactionTemplate(platformTransactionManager);
    }

}
