package org.onap.ccsdk.config.data.adaptor.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import javax.sql.DataSource;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class DataSourceWrap implements DataSource {

    private static EELFLogger logger = EELFManager.getInstance().getLogger(DataSourceWrap.class);

    private DataSource dataSource;

    public DataSourceWrap(DataSource dataSource) {
        logger.info("Setting Data Source {} ", dataSource);
        this.dataSource = dataSource;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }

    @SuppressWarnings("squid:S2095")
    @Override
    public Connection getConnection() throws SQLException {
        Connection c = dataSource.getConnection();
        logger.trace("getConnection: ({})", c.getClass().getName());
        c.setAutoCommit(true);
        return c;
    }

    @SuppressWarnings("squid:S2095")
    @Override
    public Connection getConnection(String username, String pass) throws SQLException {
        Connection c = dataSource.getConnection(username, pass);
        c.setAutoCommit(true);
        return c;
    }


}
