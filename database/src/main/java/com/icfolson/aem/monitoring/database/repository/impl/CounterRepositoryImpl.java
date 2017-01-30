package com.icfolson.aem.monitoring.database.repository.impl;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.SystemInfo;
import com.icfolson.aem.monitoring.database.SystemInfoProvider;
import com.icfolson.aem.monitoring.database.repository.CounterRepository;
import com.icfolson.aem.monitoring.database.writer.CountersDatabase;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import java.util.List;
import java.util.Map;

@Service
@Component(immediate = true)
public class CounterRepositoryImpl implements CounterRepository {

    @Reference
    private SystemInfoProvider systemInfoProvider;

    @Reference
    private ConnectionProvider connectionProvider;
    private CountersDatabase database;

    @Override
    public Map<String, Short> getCounterNameHierarchy() {
        return database.getCounterNameHierarchy();
    }

    @Override
    public void writeCounter(final MonitoringCounter counter) {
        database.writeCounter(counter);
    }

    @Override
    public List<MonitoringCounter> getCounters(final Long since, final Integer limit) {
        return null;
    }

    @Activate
    @Modified
    protected final void modified() {
        final SystemInfo systemInfo = systemInfoProvider.getSystemInfo();
        database = new CountersDatabase(systemInfo.getSystemId(), connectionProvider);
    }

}
