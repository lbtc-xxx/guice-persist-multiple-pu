package mypkg;

import mypkg.Constants;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class DatabaseInitializeContextListener implements ServletContextListener {

    private static final Driver DRIVER = new org.apache.derby.jdbc.EmbeddedDriver();

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        try {
            DriverManager.registerDriver(DRIVER);
            populateDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void populateDatabase() throws SQLException {
        final String master = Constants.MASTER;
        final String slave = Constants.SLAVE;

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

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (final SQLException expected) {
            // nop
        }
        try {
            DriverManager.deregisterDriver(DRIVER);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
