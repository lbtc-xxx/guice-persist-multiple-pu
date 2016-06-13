package mypkg;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import javax.persistence.EntityManager;

// https://groups.google.com/forum/#!topic/google-guice/OMxfc1PCKvw
// https://groups.google.com/forum/#!topic/google-guice/2VK-bdsnjZc/discussion
public class MyGuiceServletConfig extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new MyServletModule());
    }

    private static class MyServletModule extends ServletModule {
        @Override
        protected void configureServlets() {
            install(new MasterPrivateModule());
            install(new SlavePrivateModule());

            filter("/*").through(MasterPrivateModule.KEY);
            filter("/*").through(SlavePrivateModule.KEY);

            serve("/master").with(MasterServlet.class);
            serve("/slave").with(SlaveServlet.class);
            serve("/slave2").with(SlaveServlet2.class);

//            serve("/no").with(NoAnnotationServlet.class);
        }
    }

    private static class MasterPrivateModule extends PrivateModule {
        private static final Key<PersistFilter> KEY = Key.get(PersistFilter.class, UseMaster.class);

        @Override
        protected void configure() {
            install(new JpaPersistModule("masterPU"));
            bind(MyTableService.class).annotatedWith(UseMaster.class).to(MyTableService.class);
            expose(MyTableService.class).annotatedWith(UseMaster.class);

            bind(KEY).to(PersistFilter.class);
            expose(KEY);

            // unfortunately this overwrites slave binding
//            bind(MyTableService.class);
//            expose(MyTableService.class);
        }
    }

    private static class SlavePrivateModule extends PrivateModule {
        private static final Key<PersistFilter> KEY = Key.get(PersistFilter.class, UseSlave.class);

        @Override
        protected void configure() {
            install(new JpaPersistModule("slavePU"));
            bind(MyTableService.class).annotatedWith(UseSlave.class).to(MyTableService.class);
            expose(MyTableService.class).annotatedWith(UseSlave.class);

            bind(KEY).to(PersistFilter.class);
            expose(KEY);

            final Provider<EntityManager> entityManagerProvider = binder().getProvider(EntityManager.class);
            bind(EntityManager.class).annotatedWith(UseSlave.class).toProvider(entityManagerProvider);
            expose(EntityManager.class).annotatedWith(UseSlave.class);
        }
    }
}
