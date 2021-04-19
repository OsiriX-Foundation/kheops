package online.kheops.auth_server.fetch;

import online.kheops.auth_server.PepAccessTokenBuilder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class FetchContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(FetchContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        PepAccessTokenBuilder.setSecret(sce.getServletContext().getInitParameter("online.kheops.auth.hmacsecret"));

        try {
            Fetcher.setDicomWebURI(new URI(sce.getServletContext().getInitParameter("online.kheops.pacs.uri")));
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, "URI in context param online.kheops.pacs.uri is not valid", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) { }
}
