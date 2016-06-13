package mypkg;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
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
    }

    private Injector injector;

    @Test
    public void masterShouldBeInjectedWhenNoAnnotationSupplied() {
        final EntityManager em = injector.getInstance(EntityManager.class);

        assertThat(em.getProperties().get("javax.persistence.jdbc.url"), is("jdbc:derby:memory:masterDB;create=true"));
    }

    @Test
    public void slaveShouldBeInjectedWhenSlaveDatabaseAnnotationSupplied() {
        final EntityManager em = injector.getInstance(Key.get(EntityManager.class, SlaveDatabase.class));

        assertThat(em.getProperties().get("javax.persistence.jdbc.url"), is("jdbc:derby:memory:slaveDB;create=true"));
    }
}
