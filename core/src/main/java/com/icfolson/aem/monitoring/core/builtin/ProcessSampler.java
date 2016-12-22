package com.icfolson.aem.monitoring.core.builtin;

import com.icfolson.aem.monitoring.core.service.MonitoringService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

@Service
@Component(immediate = true, metatype = true, label = "AEM Monitoring: Java process sampler")
public class ProcessSampler implements Runnable {

    private static final String PROCESS = "Process";
    private static final String MEMORY = "Memory";
    private static final String HEAP = "Heap";
    private static final String COMMITTED = "Committed";
    private static final String MEGABYTES = "MiB";
    private static final String USED = "Used";
    private static final String NON_HEAP = "NonHeap";
    private static final String CPU = "CPU";
    private static final String AVERAGE_LOAD = "AverageLoad";

    private static final String[] COMMITTED_HEAP = new String[]{PROCESS, MEMORY, HEAP, COMMITTED, MEGABYTES};
    private static final String[] USED_HEAP = new String[]{PROCESS, MEMORY, HEAP, USED, MEGABYTES};
    private static final String[] COMMITTED_NON_HEAP = new String[]{PROCESS, MEMORY, NON_HEAP, COMMITTED, MEGABYTES};
    private static final String[] USED_NON_HEAP = new String[]{PROCESS, MEMORY, NON_HEAP, USED, MEGABYTES};
    private static final String[] AVERAGE_CPU_LOAD = new String[]{PROCESS, CPU, AVERAGE_LOAD};

    private static final float MEGABYTE_IN_BYTES = 1f / (1024f * 1024f);

    private static final int DEFAULT_SAMPLE_INTERVAL_SECONDS = 15;

    @Property(name = "scheduler.period", longValue = DEFAULT_SAMPLE_INTERVAL_SECONDS)
    private static final String SAMPLE_INTERVAL_SECONDS_PROP = "sample.interval.seconds";

    @Reference
    private MonitoringService service;

    @Override
    public void run() {
        final MemoryUsage heapMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        service.recordMetric(COMMITTED_HEAP, MEGABYTE_IN_BYTES * heapMemory.getCommitted());
        service.recordMetric(USED_HEAP, MEGABYTE_IN_BYTES * heapMemory.getUsed());
        final MemoryUsage nonHeapMemory = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        service.recordMetric(COMMITTED_NON_HEAP, MEGABYTE_IN_BYTES * nonHeapMemory.getCommitted());
        service.recordMetric(USED_NON_HEAP, MEGABYTE_IN_BYTES * nonHeapMemory.getUsed());
        final Double loadAverage = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        service.recordMetric(AVERAGE_CPU_LOAD, loadAverage.floatValue());
    }
}
