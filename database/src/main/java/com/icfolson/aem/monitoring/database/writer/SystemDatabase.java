package com.icfolson.aem.monitoring.database.writer;

import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.SystemInfo;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.SystemProperty;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemRecord;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemDatabase {

    private static final Logger LOG = LoggerFactory.getLogger(SystemDatabase.class);

    private final ConnectionProvider connectionProvider;

    public SystemDatabase(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void writeSystem(final SystemInfo systemInfo) {
        try (DSLContext context = getContext()) {
            SystemRecord systemRecord = context
                .selectFrom(Tables.SYSTEM)
                .where(Tables.SYSTEM.SYSTEM_ID.eq(systemInfo.getSystemId()))
                .fetchAny();
            if (systemRecord == null) {
                systemRecord = context.newRecord(Tables.SYSTEM);
                systemRecord.setSystemId(systemInfo.getSystemId());
                systemRecord.insert();
            }
            context.deleteFrom(SystemProperty.SYSTEM_PROPERTY)
                .where(SystemProperty.SYSTEM_PROPERTY.SYSTEM_ID.eq(systemInfo.getSystemId()))
                .execute();
            for (final String name : systemInfo.getPropertyNames()) {
                SystemPropertyRecord propertyRecord = context
                    .selectFrom(Tables.SYSTEM_PROPERTY)
                    .where(
                        SystemProperty.SYSTEM_PROPERTY.SYSTEM_ID.eq(systemInfo.getSystemId())
                            .and(SystemProperty.SYSTEM_PROPERTY.NAME.eq(name))
                    ).fetchAny();
                if (propertyRecord == null) {
                    propertyRecord = context.newRecord(Tables.SYSTEM_PROPERTY);
                    propertyRecord.setSystemId(systemInfo.getSystemId());
                    propertyRecord.setName(name);
                    propertyRecord.setValue(systemInfo.getPropertyValue(name));
                    propertyRecord.insert();
                }
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error writing system data", e);
        }
    }

    private DSLContext getContext() throws MonitoringDBException {
        return DSL.using(connectionProvider.getConnection(), SQLDialect.H2);
    }

}
