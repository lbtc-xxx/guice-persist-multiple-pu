package mypkg;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
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

            final Provider<EntityManager> entityManagerProvider = binder().getProvider(EntityManager.class);
            bind(EntityManager.class).annotatedWith(MasterDatabase.class).toProvider(entityManagerProvider);
            expose(EntityManager.class).annotatedWith(MasterDatabase.class);

            bind(PersistService.class).annotatedWith(MasterDatabase.class).toProvider(binder().getProvider(PersistService.class));
            expose(PersistService.class).annotatedWith(MasterDatabase.class);

            bind(UnitOfWork.class).annotatedWith(MasterDatabase.class).toProvider(binder().getProvider(UnitOfWork.class));
            expose(UnitOfWork.class).annotatedWith(MasterDatabase.class);

            // service classes that use EntityManager or @Transactional must be bind explicitly here
            //
            // http://stackoverflow.com/questions/8486437/guice-beginner-how-to-bind-concrete-classes
            bind(MyService.class);
            expose(MyService.class);
        }
    }

    private static class SlavePrivateModule extends PrivateModule {
        @Override
        protected void configure() {
            install(new JpaPersistModule("slavePU"));

            final Provider<EntityManager> entityManagerProvider = binder().getProvider(EntityManager.class);
            bind(EntityManager.class).annotatedWith(SlaveDatabase.class).toProvider(entityManagerProvider);
            expose(EntityManager.class).annotatedWith(SlaveDatabase.class);

            bind(PersistService.class).annotatedWith(SlaveDatabase.class).toProvider(binder().getProvider(PersistService.class));
            expose(PersistService.class).annotatedWith(SlaveDatabase.class);

            bind(UnitOfWork.class).annotatedWith(SlaveDatabase.class).toProvider(binder().getProvider(UnitOfWork.class));
            expose(UnitOfWork.class).annotatedWith(SlaveDatabase.class);
        }
    }
}
