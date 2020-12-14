package online.kheops.auth_server;

import online.kheops.auth_server.webhook.delayed_webhook.DelayedWebhook;
import online.kheops.auth_server.webhook.delayed_webhook.DelayedWebhookImpl;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

public class ApplicationBinder extends AbstractBinder {


    @Override
    protected void configure() {
        bind(DelayedWebhookImpl.class).to(DelayedWebhook.class).in(Singleton.class);
        bind(KheopsInstance.class).to(KheopsInstance.class).in(Singleton.class);
    }
}
