package online.kheops.auth_server.liquibase;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LiquibaseContextListener implements ServletContextListener {

    private static ServletContext servletContext;
    private final String changeLogFile = "kheopsChangeLog-master.xml";
    private static final java.util.logging.Logger LOG = Logger.getLogger(LiquibaseContextListener.class.getName());
    private static final String dbVersion = "v1";

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        servletContext = sce.getServletContext();

        LOG.log(Level.INFO, "Start initializing the DB with Liquibase. Database version : " + dbVersion);

        final Properties properties = new Properties();
        properties.setProperty(Environment.USER, getJDBCUser());
        properties.setProperty(Environment.PASS, getJDBCPassword());
        properties.setProperty(Environment.URL, getJDBCUrl());
        properties.setProperty(Environment.DRIVER, "org.postgresql.Driver");
        properties.setProperty(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty(Environment.SHOW_SQL, "false");

        final Configuration cfg = new Configuration();
        cfg.setProperties(properties);

            try {

                // Prepare the Hibernate configuration
                StandardServiceRegistry reg = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build();
                MetadataSources metaDataSrc = new MetadataSources(reg);

                // Get database connection
                Connection con = metaDataSrc.getServiceRegistry().getService(ConnectionProvider.class).getConnection();
                JdbcConnection jdbcCon = new JdbcConnection(con);

                // Initialize Liquibase and run the update
                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcCon);
                Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database);
                final String version = dbVersion;

                if (liquibase.tagExists(version)) {
                    liquibase.rollback(version, "");
                    liquibase.update(version, "");
                } else {
                    liquibase.update(version, "");
                }
                liquibase.validate();
                jdbcCon.close();
                con.close();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Unable to use liquibase", e);
            }
        LOG.log(Level.INFO, "Liquibase : database version : " + dbVersion + "SUCCESSFUL");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        servletContext = null;
    }

    private static void verifyState() {
        if (servletContext == null) {
            throw new IllegalStateException("Getting parameters before the context is initialized");
        }
    }

    private static String getJDBCUrl() {
        verifyState();
        return servletContext.getInitParameter("online.kheops.jdbc.url");
    }
    private static String getJDBCPassword() {
        verifyState();
        return servletContext.getInitParameter("online.kheops.jdbc.password");
    }
    private static String getJDBCUser() {
        verifyState();
        return servletContext.getInitParameter("online.kheops.jdbc.user");
    }

}
