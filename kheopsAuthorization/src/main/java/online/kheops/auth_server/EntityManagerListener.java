package online.kheops.auth_server;

import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebListener
public class EntityManagerListener implements ServletContextListener {
    private static EntityManagerFactory emf;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Map<String, String> properties = new HashMap<>();

        properties.put("javax.persistence.jdbc.user", event.getServletContext().getInitParameter("online.kheops.jdbc.user"));
        properties.put("javax.persistence.jdbc.password", event.getServletContext().getInitParameter("online.kheops.jdbc.password"));
        properties.put("javax.persistence.jdbc.url", event.getServletContext().getInitParameter("online.kheops.jdbc.url"));

        emf = Persistence.createEntityManagerFactory("online.kheops", properties);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        emf.close();
    }

    public static EntityManager createEntityManager() {
        if (emf == null) {
            throw new IllegalStateException("Context is not initialized yet.");
        }
        return emf.createEntityManager();
    }

    public static Connection getConnection() throws SQLException {
        return emf.unwrap(SessionFactory.class).
                getSessionFactoryOptions().
                getServiceRegistry().
                getService(ConnectionProvider.class).
                getConnection();
    }

}
