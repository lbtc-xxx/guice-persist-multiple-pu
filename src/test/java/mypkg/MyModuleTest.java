package mypkg;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import nestedservice.AlwaysSlaveService;
import nestedservice.BarService;
import nestedservice.FooService;
import nestedservice.BazService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MyModuleTest {

    @BeforeClass
    public static void initClass() throws Exception {
        populateDatabase();
    }

    private static void populateDatabase() throws SQLException {
        final String master = "jdbc:derby:memory:masterDB;create=true";
        final String slave = "jdbc:derby:memory:slaveDB;create=true";

        for (final String url : Arrays.asList(master, slave)) {
            createTable(url);
        }

        insert(master, "master");
        insert(slave, "slave");
    }

    private static void createTable(final String url) throws SQLException {
        final String ddl = "create table mytable (mycol varchar (255), primary key (mycol))";
        try (final Connection cn = DriverManager.getConnection(url);
             final Statement st = cn.createStatement()) {
            st.executeUpdate(ddl);
        }
    }

    private static void insert(final String url, final String value) throws SQLException {
        final String sql = "insert into mytable (mycol) values (?)";
        try (final Connection cn = DriverManager.getConnection(url);
             final PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.executeUpdate();
        }
    }

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new MyModule());
        injector.getInstance(Key.get(PersistService.class, MasterDatabase.class)).start();
        injector.getInstance(Key.get(PersistService.class, SlaveDatabase.class)).start();
        injector.getInstance(Key.get(UnitOfWork.class, MasterDatabase.class)).begin();
        injector.getInstance(Key.get(UnitOfWork.class, SlaveDatabase.class)).begin();
    }

    private Injector injector;

    @Test
    public void alwaysSlaveServiceUsesSlaveDatabase() {
        final AlwaysSlaveService sut = injector.getInstance(AlwaysSlaveService.class);

        final String actual = sut.find();

        assertThat(actual, is("slave"));
    }

    @Test
    public void barServiceUsesDistinctDatabase() {
        final BarService masterSut = injector.getInstance(Key.get(BarService.class, MasterDatabase.class));
        final BarService slaveSut = injector.getInstance(Key.get(BarService.class, SlaveDatabase.class));

        final String masterActual = masterSut.find();
        final String slaveActual = slaveSut.find();

        assertThat(masterActual, is("master"));
        assertThat(slaveActual, is("slave"));
    }

    @Test
    public void fooServiceUsesDistinctDatabase() {
        final FooService masterSut = injector.getInstance(Key.get(FooService.class, MasterDatabase.class));
        final FooService slaveSut = injector.getInstance(Key.get(FooService.class, SlaveDatabase.class));

        final String masterActual = masterSut.find();
        final String slaveActual = slaveSut.find();

        assertThat(masterActual, is("master"));
        assertThat(slaveActual, is("slave"));
    }

    @Test
    public void bazServiceUsesMasterDatabase() {
        final BazService sut = injector.getInstance(BazService.class);

        sut.save();

        assertThat(injector.getInstance(Key.get(EntityManager.class, MasterDatabase.class)).createQuery("select count(e) from MyTable e", Long.class).getSingleResult(), is(3L));
        assertThat(injector.getInstance(Key.get(EntityManager.class, SlaveDatabase.class)).createQuery("select count(e) from MyTable e", Long.class).getSingleResult(), is(1L));
    }

    @After
    public void tearDown() throws Exception {
        injector.getInstance(Key.get(UnitOfWork.class, MasterDatabase.class)).end();
        injector.getInstance(Key.get(UnitOfWork.class, SlaveDatabase.class)).end();
    }
}
