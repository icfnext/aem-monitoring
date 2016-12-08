package com.icfolson.aem.monitoring.h2;

import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
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
@Component(immediate = true, metatype = true)
public class H2ConnectionProvider implements ConnectionProvider {

    private static final String SCHEME = "jdbc:h2:";
    private static final String SERVER_DEFAULT = "tcp://localhost:8084";
    private static final String URL_DEFAULT_POSTFIX = "/db/monitoring;ALIAS_COLUMN_NAME=TRUE";
    private static final String USER_DEFAULT = "sa";
    private static final String PASSWORD_DEFAULT = "";

    @Reference
    private SlingSettingsService settingsService;

    @Property(label = "Server / Port")
    private static final String SERVER_PROP = "server";

    @Property(label = "Path")
    private static final String PATH_PROP = "path";

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
        final String server = PropertiesUtil.toString(properties.get(SERVER_PROP), SERVER_DEFAULT);
        final String path = PropertiesUtil.toString(properties.get(PATH_PROP),
            settingsService.getSlingHomePath() + URL_DEFAULT_POSTFIX);
        final String user = PropertiesUtil.toString(properties.get(USER_PROP), USER_DEFAULT);
        final String password = PropertiesUtil.toString(properties.get(PASSWORD_PROP), PASSWORD_DEFAULT);
        dataSource = new JdbcDataSource();
        dataSource.setURL(SCHEME + server + (path.startsWith("/") ? "" : "/") + path);
        dataSource.setUser(user);
        dataSource.setPassword(password);
    }

}
