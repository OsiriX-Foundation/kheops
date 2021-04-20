package online.kheops.auth_server.liquibase;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import online.kheops.auth_server.EntityManagerListener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.exit;

public class LiquibaseContextListener implements ServletContextListener {

    private static final String CHANGE_LOG_FILE = "kheopsChangeLog-master.xml";
    private static final java.util.logging.Logger LOG = Logger.getLogger(LiquibaseContextListener.class.getName());
    private static final String DB_VERSION = "v3.6";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.log(Level.INFO, "Start initializing the DB with Liquibase. Database version : " + DB_VERSION);

        JdbcConnection jdbcCon = null;
        try (final Connection con = EntityManagerListener.getConnection()) {
            jdbcCon = new JdbcConnection(con);

            // Initialize Liquibase and run the update
            final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcCon);
            try (final Liquibase liquibase = new Liquibase(CHANGE_LOG_FILE, new ClassLoaderResourceAccessor(), database)) {

                final String version = DB_VERSION;

                if(liquibase.tagExists("v1.0") && !liquibase.tagExists("v3.0")) {
                    LOG.log(Level.SEVERE, "WARNING before installing this version of KHEOPS, install the version v0.9.3");
                    exit(1);
                }

                if (liquibase.tagExists(version)) {
                    liquibase.rollback(version, "");
                    liquibase.update(version, "");
                } else {
                    liquibase.update(version, "");
                }
                liquibase.validate();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Unable to use liquibase", e);
                exit(1);
            } finally {
                if (jdbcCon != null && !jdbcCon.isClosed()) {
                    jdbcCon.close();
                }
            }
        } catch (SQLException | DatabaseException e) {
            LOG.log(Level.SEVERE, "Unable to use liquibase", e);
            exit(1);
        }
        LOG.log(Level.INFO, "Liquibase : database version : " + DB_VERSION + " SUCCESSFUL");
    }
}
