package com.icfolson.aem.monitoring.database.repository.impl;

import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.SystemInfo;
import com.icfolson.aem.monitoring.database.SystemInfoProvider;
import com.icfolson.aem.monitoring.database.writer.SystemDatabase;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class SystemRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SystemRepository.class);

    @Reference
    private ConnectionProvider connectionProvider;

    @Reference
    private SystemInfoProvider systemInfoProvider;

    @Activate
    @Modified
    protected final void modified() {
        final SystemInfo systemInfo = systemInfoProvider.getSystemInfo();
        final SystemDatabase systemDatabase = new SystemDatabase(connectionProvider);
        systemDatabase.writeSystem(systemInfo);
    }

}
