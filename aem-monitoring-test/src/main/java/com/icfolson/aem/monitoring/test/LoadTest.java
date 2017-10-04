package com.icfolson.aem.monitoring.test;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.model.base.DefaultMonitoringEvent;
import com.icfolson.aem.monitoring.core.service.MonitoringService;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.commons.threads.ThreadPool;

import java.util.Date;
import java.util.Random;

public class LoadTest implements Runnable {

    private static final QualifiedName NAME = new QualifiedName("test");

    private static final String STRING_PROP_TEMPLATE = "string.property.%s";
    private static final String STRING_VALUE_TEMPLATE = "value.%s";
    private static final String REAL_PROP_TEMPLATE = "real.property.%s";

    private static final Random RANDOM = new Random();

    private final MonitoringService service;
    private final Scheduler scheduler;
    private final ThreadPool threadPool;

    private int eventsPerSecond = 100;
    private int testDuration = 60;
    private int stringProperties = 10;
    private int stringPropertyValues = 10;
    private int realProperties = 2;

    public LoadTest(MonitoringService service, Scheduler scheduler, ThreadPool threadPool) {
        this.service = service;
        this.scheduler = scheduler;
        this.threadPool = threadPool;
    }

    public void start() {
        final ScheduleOptions options = scheduler.AT(new Date(), testDuration, 1L);
        scheduler.schedule(this, options);
    }

    public void run() {
        threadPool.submit(() -> {
            for (int i = 0; i < eventsPerSecond; i++) {
                service.recordEvent(generateEvent());
            }
        });
    }

    public void setEventsPerSecond(int eventsPerSecond) {
        this.eventsPerSecond = eventsPerSecond;
    }

    public void setTestDuration(int testDuration) {
        this.testDuration = testDuration;
    }

    public void setStringProperties(int stringProperties) {
        this.stringProperties = stringProperties;
    }

    public void setStringPropertyValues(int stringPropertyValues) {
        this.stringPropertyValues = stringPropertyValues;
    }

    public void setRealProperties(int realProperties) {
        this.realProperties = realProperties;
    }

    private MonitoringEvent generateEvent() {
        DefaultMonitoringEvent event = new DefaultMonitoringEvent(NAME);
        for (int i = 0; i < stringProperties; i++) {
            final String name = String.format(STRING_PROP_TEMPLATE, i);
            final String value = String.format(STRING_VALUE_TEMPLATE, RANDOM.nextInt(stringPropertyValues));
            event.setProperty(name, value);
        }
        for (int i = 0; i < realProperties; i++) {
            final String name = String.format(REAL_PROP_TEMPLATE, i);
            final Float value = RANDOM.nextFloat();
            event.setProperty(name, value);
        }
        return event;
    }


}
