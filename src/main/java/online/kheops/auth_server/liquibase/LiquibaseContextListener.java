package online.kheops.auth_server.liquibase;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import online.kheops.auth_server.util.JOOQTools;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;

import static online.kheops.auth_server.util.JOOQTools.getDataSource;

public class LiquibaseContextListener implements ServletContextListener {

    private static ServletContext servletContext;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        servletContext = sce.getServletContext();
        try {
            // Prepare the Hibernate configuration
            StandardServiceRegistry reg = new StandardServiceRegistryBuilder().configure().build();
            MetadataSources metaDataSrc = new MetadataSources(reg);

            // Get database connection
            Connection con = metaDataSrc.getServiceRegistry().getService(ConnectionProvider.class).getConnection();
            JdbcConnection jdbcCon = new JdbcConnection(con);

            // Initialize Liquibase and run the update
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcCon);
            Liquibase liquibase = new Liquibase("kheopsChangeLog-master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update("v1");
        } catch (Exception e) {
            e.printStackTrace();
        }


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
