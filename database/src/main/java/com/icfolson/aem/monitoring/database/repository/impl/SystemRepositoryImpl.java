package com.icfolson.aem.monitoring.database.repository.impl;

import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.repository.SystemRepository;
import com.icfolson.aem.monitoring.database.system.SystemInfo;
import com.icfolson.aem.monitoring.database.system.SystemInfoProvider;
import com.icfolson.aem.monitoring.database.writer.SystemDatabase;
import org.apache.felix.scr.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Component(immediate = true)
public class SystemRepositoryImpl implements SystemRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SystemRepository.class);

    @Reference
    private ConnectionProvider connectionProvider;

    @Reference
    private SystemInfoProvider systemInfoProvider;

    private SystemDatabase systemDatabase;

    @Override public short getSystemId(final String repositoryUuid) throws MonitoringDBException {
        return systemDatabase.getId(repositoryUuid);
    }

    @Override
    public String getRepositoryUuid(short systemId) throws MonitoringDBException {
        return systemDatabase.getRepositoryUuid(systemId);
    }

    @Activate
    @Modified
    protected final void modified() {
        final SystemInfo systemInfo = systemInfoProvider.getSystemInfo();
        systemDatabase = new SystemDatabase(connectionProvider);
        systemDatabase.writeSystem(systemInfo);
    }

}
