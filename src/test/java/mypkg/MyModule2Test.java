package mypkg;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;
import nestedservice.BarService;
import nestedservice.BazService;
import nestedservice.FooService;
import nestedservice.MasterBazService;
import nestedservice.SlaveBazService;
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

public class MyModule2Test {

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
        injector = Guice.createInjector(new MyModule2());
        injector.getInstance(Key.get(PersistService.class, MasterDatabase.class)).start();
        injector.getInstance(Key.get(PersistService.class, SlaveDatabase.class)).start();
        injector.getInstance(Key.get(UnitOfWork.class, MasterDatabase.class)).begin();
        injector.getInstance(Key.get(UnitOfWork.class, SlaveDatabase.class)).begin();
    }

    private Injector injector;

    @Test
    public void masterBazServiceUsesMaster() throws Exception {
        final MasterBazService sut = injector.getInstance(MasterBazService.class);

        final String foo = sut.findViaFoo();
        final String bar = sut.findViaBar();

        assertThat(foo, is("master"));
        assertThat(bar, is("master"));
    }

    @Test
    public void slaveBazServiceUsesMaster() throws Exception {
        final SlaveBazService sut = injector.getInstance(SlaveBazService.class);

        final String foo = sut.findViaFoo();
        final String bar = sut.findViaBar();

        assertThat(foo, is("slave"));
        assertThat(bar, is("slave"));
    }

    @After
    public void tearDown() throws Exception {
        injector.getInstance(Key.get(UnitOfWork.class, MasterDatabase.class)).end();
        injector.getInstance(Key.get(UnitOfWork.class, SlaveDatabase.class)).end();
    }
}
