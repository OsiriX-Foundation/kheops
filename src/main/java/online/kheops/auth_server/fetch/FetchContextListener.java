package online.kheops.auth_server.fetch;


import online.kheops.auth_server.PACSAuthTokenBuilder;
import online.kheops.auth_server.assertion.AssertionVerifier;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class FetchContextListener implements ServletContextListener {
    private static final int MAXIMUM_CONCURRENT = 1;

    private static final Logger LOG = Logger.getLogger(FetchContextListener.class.getName());

    private ScheduledThreadPoolExecutor executor = null;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        executor = new ScheduledThreadPoolExecutor(MAXIMUM_CONCURRENT);

        AssertionVerifier.setSecrets(sce.getServletContext().getInitParameter("online.kheops.superuser.hmacsecret"),
                sce.getServletContext().getInitParameter("online.kheops.auth.hmacsecret"));
        PACSAuthTokenBuilder.setSecret(sce.getServletContext().getInitParameter("online.kheops.auth.hmacsecret"));

        try {
            Fetcher.setDicomWebURI(new URI(sce.getServletContext().getInitParameter("online.kheops.pacs.uri")));
            FetchTask task = new FetchTask(new URI(sce.getServletContext().getInitParameter("online.kheops.pacs.uri")));
            executor.scheduleAtFixedRate(task, 0, 2, TimeUnit.SECONDS);
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, "URI in context param online.kheops.pacs.uri is not valid", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        executor.shutdown();
    }
}
