package com.icfolson.aem.monitoring.h2;

import com.google.common.io.CharStreams;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.connection.ConnectionWrapper;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.settings.SlingSettingsService;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.SQLDialect;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Dictionary;

@Service
@Component(immediate = true, metatype = true, policy = ConfigurationPolicy.REQUIRE)
public class H2ConnectionProvider implements ConnectionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(H2ConnectionProvider.class);

    private static final SQLDialect DIALECT = SQLDialect.H2;

    private static final String DEFAULT_NAME = "monitoring";
    private static final String DB_ARGS = ";ALIAS_COLUMN_NAME=TRUE";
    private static final String USER_DEFAULT = "sa";
    private static final String PASSWORD_DEFAULT = "";

    @Reference
    private SlingSettingsService settingsService;

    @Reference
    private DatabaseServer server;

    @Property(label = "DB Name")
    private static final String DB_NAME_PROP = "name";

    @Property(label = "User")
    private static final String USER_PROP = "user";

    @Property(label = "Password")
    private static final String PASSWORD_PROP = "password";

    private JdbcDataSource dataSource;

    @Override
    public ConnectionWrapper getConnection() throws MonitoringDBException {
        try {
            final Connection connection = dataSource == null ? null : dataSource.getConnection();
            return connection == null ? null : new ConnectionWrapper(connection, DIALECT);
        } catch (SQLException e) {
            throw new MonitoringDBException(e);
        }
    }

    @Activate
    @Modified
    protected final void activate(final ComponentContext context) {
        final Dictionary properties = context.getProperties();
        final String name = PropertiesUtil.toString(properties.get(DB_NAME_PROP), DEFAULT_NAME);
        final String user = PropertiesUtil.toString(properties.get(USER_PROP), USER_DEFAULT);
        final String password = PropertiesUtil.toString(properties.get(PASSWORD_PROP), PASSWORD_DEFAULT);
        dataSource = new JdbcDataSource();
        dataSource.setURL(server.getConnectionURL() + "/" + name + DB_ARGS);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        final URL fileUrl = context.getBundleContext().getBundle().getEntry("/init.sql");
        if (fileUrl != null) {
            try (final InputStream in = fileUrl.openStream()) {
                final InputStreamReader inr = new InputStreamReader(in, "utf-8");
                final String sql = CharStreams.toString(inr);
                initDatabase(sql);
            } catch (IOException e) {
                LOG.error("Error reading SQL init file", e);
            }
        }
    }

    private void initDatabase(final String sql) {
        try (ConnectionWrapper connection = getConnection()) {
            final int results = connection.getContext().execute(sql);
            LOG.info("H2 database initialized with {} results", results);
        } catch (MonitoringDBException e) {
            LOG.error("Error initializing database", e);
        }
    }

}
