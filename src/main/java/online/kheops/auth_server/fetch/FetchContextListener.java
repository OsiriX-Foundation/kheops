package online.kheops.auth_server.fetch;


import online.kheops.auth_server.assertion.AssertionVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@WebListener
public class FetchContextListener implements ServletContextListener {
    private static final int MAXIMUM_CONCURRENT = 1;

    private static final Logger LOG = LoggerFactory.getLogger(FetchContextListener.class);

    private ScheduledThreadPoolExecutor executor = null;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        executor = new ScheduledThreadPoolExecutor(MAXIMUM_CONCURRENT);

        AssertionVerifier.setSuperuserSecret(sce.getServletContext().getInitParameter("online.kheops.superuser.hmacsecret"));

        try {
            FetchTask task = new FetchTask(new URI(sce.getServletContext().getInitParameter("online.kheops.pacs.uri")));
            executor.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
        } catch (URISyntaxException e) {
            LOG.error("URI in context param online.kheops.pacs.uri is not valid", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        executor.shutdown();
    }
}
