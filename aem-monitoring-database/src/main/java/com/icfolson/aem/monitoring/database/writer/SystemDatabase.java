package com.icfolson.aem.monitoring.database.writer;

import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.connection.ConnectionWrapper;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.SystemProperty;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemRecord;
import com.icfolson.aem.monitoring.database.system.SystemInfo;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SystemDatabase {

    private static final Logger LOG = LoggerFactory.getLogger(SystemDatabase.class);

    private final Map<String, Short> idCache = new ConcurrentHashMap<>(new HashMap<>());
    private final Map<Short, String> repoCache = new ConcurrentHashMap<>(new HashMap<>());

    private final ConnectionProvider connectionProvider;

    public short getId(final String repositoryUuid) throws MonitoringDBException {
        if (idCache.containsKey(repositoryUuid)) {
            return idCache.get(repositoryUuid);
        }
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            SystemRecord systemRecord = context
                    .selectFrom(Tables.SYSTEM)
                    .where(Tables.SYSTEM.REPOSITORY_UUID.eq(repositoryUuid))
                    .fetchAny();
            if (systemRecord != null) {
                final short systemId = systemRecord.getSystemId();
                idCache.put(repositoryUuid, systemId);
                return systemId;
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error writing system data", e);
        }
        throw new MonitoringDBException("System not found for Repository UUID: " + repositoryUuid);
    }

    public String getRepositoryUuid(final short systemId) throws MonitoringDBException {
        if (repoCache.containsKey(systemId)) {
            return repoCache.get(systemId);
        }
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            SystemRecord systemRecord = context
                    .selectFrom(Tables.SYSTEM)
                    .where(Tables.SYSTEM.SYSTEM_ID.eq(systemId))
                    .fetchAny();
            if (systemRecord != null) {
                final String repositoryUuid = systemRecord.getRepositoryUuid();
                repoCache.put(systemId, repositoryUuid);
                return repositoryUuid;
            }
        } catch (MonitoringDBException e) {
            LOG.error("Error writing system data", e);
        }
        throw new MonitoringDBException("System not found for system ID: " + systemId);
    }

    public SystemDatabase(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void writeSystem(final SystemInfo systemInfo) {
        final String repoUuid = systemInfo.getSystemId().toString();
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            SystemRecord systemRecord = context
                    .selectFrom(Tables.SYSTEM)
                    .where(Tables.SYSTEM.REPOSITORY_UUID.eq(repoUuid))
                    .fetchAny();
            if (systemRecord == null) {
                systemRecord = context.newRecord(Tables.SYSTEM);
                systemRecord.setRepositoryUuid(repoUuid);
                systemRecord.insert();
            }
            final short systemId = systemRecord.getSystemId();
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
