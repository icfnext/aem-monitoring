package com.icfolson.aem.monitoring.database.repository.impl;

import com.google.common.collect.BiMap;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.repository.EventRepository;
import com.icfolson.aem.monitoring.database.repository.SystemRepository;
import com.icfolson.aem.monitoring.database.system.SystemInfo;
import com.icfolson.aem.monitoring.database.system.SystemInfoProvider;
import com.icfolson.aem.monitoring.database.writer.EventsDatabase;
import org.apache.felix.scr.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Service
@Component(immediate = true)
public class EventRepositoryImpl implements EventRepository {

    private static final Logger LOG = LoggerFactory.getLogger(EventRepositoryImpl.class);

    @Reference
    private ConnectionProvider connectionProvider;

    @Reference
    private SystemInfoProvider systemInfoProvider;

    @Reference
    private SystemRepository systemRepository;
    private EventsDatabase database;

    @Override
    public BiMap<String, Short> getEventTypeMap() {
        return database.getEventTypeMap();
    }

    @Override
    public void writeEvent(final MonitoringEvent event) {
        database.writeEvent(event);
    }

    @Override
    public List<MonitoringEvent> getEvents(final Long since, final Integer limit) {
        return database.getEvents(since, limit);
    }

    @Activate
    @Modified
    protected final void activate(final Map<String, Object> props) {
        try {
            final SystemInfo systemInfo = systemInfoProvider.getSystemInfo();
            final String repositoryUuid = systemInfo.getSystemId().toString();
            final short systemId = systemRepository.getSystemId(repositoryUuid);
            database = new EventsDatabase(systemId, connectionProvider);
        } catch (MonitoringDBException e) {
            LOG.error("Error starting Counter Repository", e);
        }
    }
}
