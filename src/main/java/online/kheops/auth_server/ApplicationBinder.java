package online.kheops.auth_server;

import online.kheops.auth_server.stow.FooHashMap;
import online.kheops.auth_server.stow.FooHashMapImpl;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import javax.inject.Singleton;

public class ApplicationBinder extends AbstractBinder {


    @Override
    protected void configure() {
        bind(FooHashMapImpl.class).to(FooHashMap.class).in(Singleton.class);
        bind(KheopsInstance.class).to(KheopsInstance.class).in(Singleton.class);
    }
}
