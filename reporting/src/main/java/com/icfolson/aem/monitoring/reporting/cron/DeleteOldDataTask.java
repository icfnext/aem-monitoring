package com.icfolson.aem.monitoring.reporting.cron;

import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.reporting.repository.EventQueryRepository;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Component(immediate = true, metatype = true, label = "AEM Monitoring: Delete Old Data Task")
@Property(label = "Task Schedule", name = "scheduler.expression", value = "0 0 0 * * ?")
public class DeleteOldDataTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteOldDataTask.class);

    private static final long DEFAULT_RETAIN_DATA_FOR = 7L;

    @Property(label = "Storage Period", description = "The length of time, in days, that data should be retained", longValue = DEFAULT_RETAIN_DATA_FOR)
    private static final String RETAIN_DATA_FOR_PROP = "retain.data.for";

    @Property(label = "Disable", boolValue = false, description = "Check to disable sling request events")
    private static final String DISABLE_PROP = "disable";

    @Reference
    private EventQueryRepository repository;

    private boolean disabled;
    private long milliseconds;

    @Override
    public void run() {
        if (disabled) {
            return;
        }
        final long now = System.currentTimeMillis();
        final long cutoff = now - milliseconds;
        try {
            repository.deleteOldData(cutoff);
        } catch (MonitoringDBException e) {
            LOG.error("Error removing old data from repository", e);
        }
    }

    @Activate
    @Modified
    protected final void activate(final Map<String, Object> props) {
        disabled = PropertiesUtil.toBoolean(props.get(DISABLE_PROP), false);
        long days = PropertiesUtil.toLong(props.get(RETAIN_DATA_FOR_PROP), DEFAULT_RETAIN_DATA_FOR);
        milliseconds = TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
    }
}
