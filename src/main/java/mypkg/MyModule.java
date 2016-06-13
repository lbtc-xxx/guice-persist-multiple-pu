package mypkg;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.persist.jpa.JpaPersistModule;

import javax.persistence.EntityManager;

// https://groups.google.com/forum/#!topic/google-guice/OMxfc1PCKvw
// https://groups.google.com/forum/#!topic/google-guice/2VK-bdsnjZc/discussion
public class MyModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new MasterPrivateModule());
        install(new SlavePrivateModule());
    }

    private static class MasterPrivateModule extends PrivateModule {
        @Override
        protected void configure() {
            install(new JpaPersistModule("masterPU"));

            expose(EntityManager.class);
        }
    }

    private static class SlavePrivateModule extends PrivateModule {
        @Override
        protected void configure() {
            install(new JpaPersistModule("slavePU"));

            final Provider<EntityManager> entityManagerProvider = binder().getProvider(EntityManager.class);
            bind(EntityManager.class).annotatedWith(SlaveDatabase.class).toProvider(entityManagerProvider);
            expose(EntityManager.class).annotatedWith(SlaveDatabase.class);
        }
    }
}
