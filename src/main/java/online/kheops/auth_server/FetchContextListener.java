package online.kheops.auth_server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@WebListener
public class FetchContextListener implements ServletContextListener {
    private static final int MAXIMUM_CONCURRENT = 1;

    private ScheduledThreadPoolExecutor executor = null;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("contextInitialized");
        executor = new ScheduledThreadPoolExecutor(MAXIMUM_CONCURRENT);

        FetchTask task = new FetchTask();
        executor.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("contextDestroyed");
        executor.shutdown();
    }
}
