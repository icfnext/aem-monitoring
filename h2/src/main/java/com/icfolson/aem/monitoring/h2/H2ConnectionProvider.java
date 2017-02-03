package com.icfolson.aem.monitoring.h2;

import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.settings.SlingSettingsService;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Service
@Component(immediate = true, metatype = true, policy = ConfigurationPolicy.REQUIRE)
public class H2ConnectionProvider implements ConnectionProvider {

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
    public String getSqlVariant() {
        return "H2";
    }

    @Override
    public Connection getConnection() throws MonitoringDBException {
        try {
            return dataSource == null ? null : dataSource.getConnection();
        } catch (SQLException e) {
            throw new MonitoringDBException(e);
        }
    }

    @Activate
    @Modified
    protected final void activate(final Map<String, Object> properties) {
        final String name = PropertiesUtil.toString(properties.get(DB_NAME_PROP), DEFAULT_NAME);
        final String user = PropertiesUtil.toString(properties.get(USER_PROP), USER_DEFAULT);
        final String password = PropertiesUtil.toString(properties.get(PASSWORD_PROP), PASSWORD_DEFAULT);
        dataSource = new JdbcDataSource();
        dataSource.setURL(server.getConnectionURL() + "/" + name + DB_ARGS);
        dataSource.setUser(user);
        dataSource.setPassword(password);
    }

}
