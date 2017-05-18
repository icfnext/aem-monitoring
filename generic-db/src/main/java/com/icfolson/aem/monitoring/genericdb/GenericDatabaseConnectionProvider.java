package com.icfolson.aem.monitoring.genericdb;

import com.day.commons.datasource.poolservice.DataSourceNotFoundException;
import com.day.commons.datasource.poolservice.DataSourcePool;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.connection.ConnectionWrapper;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.jooq.SQLDialect;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Service
@Component(immediate = true, metatype = true, policy = ConfigurationPolicy.REQUIRE,
        label = "AEM Monitoring: Generic DB Connection Provider")
public class GenericDatabaseConnectionProvider implements ConnectionProvider {

    private static final String NAME = "aem-monitoring";

    @Property(label = "SQL Dialect", description = "The SQL Dialect, which must be a constant defined here: " +
            "http://www.jooq.org/javadoc/3.6.2/org/jooq/SQLDialect.html")
    private static final String DIALECT_PROP = "dialect";

    @Reference
    private DataSourcePool poolService;

    private SQLDialect dialect;

    @Override
    public ConnectionWrapper getConnection() throws MonitoringDBException {
        try {
            DataSource dataSource = (DataSource) poolService.getDataSource(NAME);
            final Connection connection = dataSource == null ? null : dataSource.getConnection();
            return connection == null ? null : new ConnectionWrapper(connection, dialect);
        } catch (SQLException | DataSourceNotFoundException e) {
            throw new MonitoringDBException(e);
        }
    }

    @Activate
    @Modified
    protected final void activate(final Map<String, Object> props) {
        final String dialectString = PropertiesUtil.toString(props.get(DIALECT_PROP), SQLDialect.DEFAULT.getName());
        dialect = SQLDialect.valueOf(dialectString);
    }
}
