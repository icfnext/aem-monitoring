package com.icfolson.aem.monitoring.test;

import com.icfolson.aem.monitoring.core.service.MonitoringService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.commons.threads.ModifiableThreadPoolConfig;
import org.apache.sling.commons.threads.ThreadPool;
import org.apache.sling.commons.threads.ThreadPoolConfig;
import org.apache.sling.commons.threads.ThreadPoolManager;

import java.util.Map;

@Service
@Component(immediate = true)
public class LoadTestServiceImpl implements LoadTestService {

    private LoadTest loadTest;

    @Reference
    private MonitoringService service;

    @Reference
    private Scheduler scheduler;

    @Reference
    private ThreadPoolManager threadPoolManager;
    private ThreadPool pool;

    public void execute() {
        loadTest.start();
    }

    public void setEventsPerSecond(int eventsPerSecond) {
        loadTest.setEventsPerSecond(eventsPerSecond);
    }

    public void setTestDuration(int testDuration) {
        loadTest.setTestDuration(testDuration);
    }

    public void setStringProperties(int stringProperties) {
        loadTest.setStringProperties(stringProperties);
    }

    public void setStringPropertyValues(int stringPropertyValues) {
        loadTest.setStringPropertyValues(stringPropertyValues);
    }

    public void setRealProperties(int realProperties) {
        loadTest.setRealProperties(realProperties);
    }

    @Activate
    protected final void activate(final Map<String, Object> props) {
        ThreadPoolConfig config = new ModifiableThreadPoolConfig();
        pool = threadPoolManager.create(config);
        loadTest = new LoadTest(service, scheduler, pool);
    }

    @Modified
    protected final void modified(final Map<String, Object> props) {
        deactivate();
        activate(props);
    }

    @Deactivate
    protected final void deactivate() {
        threadPoolManager.release(pool);
    }
}
