package com.icfolson.aem.monitoring.database.writer;

import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.system.SystemInfo;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.SystemProperty;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemRecord;
import com.icfolson.aem.monitoring.database.connection.ConnectionWrapper;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemDatabase {

    private static final Logger LOG = LoggerFactory.getLogger(SystemDatabase.class);

    private final ConnectionProvider connectionProvider;

    public SystemDatabase(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void writeSystem(final SystemInfo systemInfo) {
        final String systemId = systemInfo.getSystemId().toString();
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            SystemRecord systemRecord = context
                    .selectFrom(Tables.SYSTEM)
                    .where(Tables.SYSTEM.SYSTEM_ID.eq(systemId))
                    .fetchAny();
            if (systemRecord == null) {
                systemRecord = context.newRecord(Tables.SYSTEM);
                systemRecord.setSystemId(systemId);
                systemRecord.insert();
            }
            context.deleteFrom(SystemProperty.SYSTEM_PROPERTY)
                    .where(SystemProperty.SYSTEM_PROPERTY.SYSTEM_ID.eq(systemId))
                    .execute();
            for (final String name : systemInfo.getPropertyNames()) {
                SystemPropertyRecord propertyRecord = context.newRecord(Tables.SYSTEM_PROPERTY);
                propertyRecord.setSystemId(systemId);
                propertyRecord.setName(name);
                propertyRecord.setValue(systemInfo.getPropertyValue(name));
                propertyRecord.insert();
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error writing system data", e);
        }
    }

    private ConnectionWrapper getConnection() throws MonitoringDBException {
        return connectionProvider.getConnection();
    }

}
