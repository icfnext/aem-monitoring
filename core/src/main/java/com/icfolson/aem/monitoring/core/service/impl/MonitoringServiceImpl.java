package com.icfolson.aem.monitoring.core.service.impl;

import com.icfolson.aem.monitoring.core.constants.EventProperties;
import com.icfolson.aem.monitoring.core.filter.DefaultFilterChain;
import com.icfolson.aem.monitoring.core.filter.MonitoringFilter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringTransaction;
import com.icfolson.aem.monitoring.core.model.impl.DefaultMonitoringCounter;
import com.icfolson.aem.monitoring.core.model.impl.DefaultMonitoringTransaction;
import com.icfolson.aem.monitoring.core.model.impl.DefaultMonitoringMetric;
import com.icfolson.aem.monitoring.core.service.MonitoringService;
import com.icfolson.aem.monitoring.core.filter.MonitoringFilterChain;
import com.icfolson.aem.monitoring.core.writer.MonitoringWriter;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Constants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

@Service
@Component(immediate = true)
public class MonitoringServiceImpl implements MonitoringService, MonitoringFilterChain {

    private final ThreadLocal<DefaultMonitoringTransaction> currentTransaction = new ThreadLocal<>();

    @Reference(referenceInterface = MonitoringFilter.class, target = "(filter.context=input)",
        policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, bind = "bindFilter",
        unbind = "unbindFilter")
    private final PriorityQueue<PriorityFilter> filters = new PriorityQueue<>();

    @Reference(referenceInterface = MonitoringWriter.class, policy = ReferencePolicy.DYNAMIC,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, bind = "bindWriter", unbind = "unbindWriter")
    private final List<MonitoringWriter> writers = new ArrayList<>();

    @Override
    public void initializeTransaction(final String name) {
        if (currentTransaction.get() != null) {
            //throw new IllegalStateException("A transaction already exists for the current thread");
        }
        currentTransaction.set(new DefaultMonitoringTransaction(name));
    }

    @Override
    public String getTransactionType() {
        final MonitoringTransaction transaction = currentTransaction.get();
        return transaction != null ? transaction.getType() : null;
    }

    @Override
    public Map<String, Object> getTransactionProperties() {
        final MonitoringTransaction transaction = currentTransaction.get();
        return transaction != null ? Collections.unmodifiableMap(transaction.getProperties()) : null;
    }

    @Override
    public void setTransactionProperty(final String name, final Object value) {
        final MonitoringTransaction transaction = currentTransaction.get();
        if (transaction == null) {
            throw new IllegalStateException("A transaction has not been initialized for the current thread");
        }
        transaction.setProperty(name, value);
    }

    @Override
    public void recordTransaction() {
        final DefaultMonitoringTransaction transaction = currentTransaction.get();
        if (transaction == null) {
            throw new IllegalStateException("A transaction has not been initialized for the current thread");
        }
        transaction.complete();
        currentTransaction.remove();
        final LocalDateTime startTime = transaction.getStartTime();
        final LocalDateTime endTime = transaction.getTimestamp();
        final long length = startTime.until(endTime, ChronoUnit.MILLIS);
        transaction.setProperty(EventProperties.TRANSACTION_LENGTH_MS, length);
        recordEvent(transaction);
    }

    @Override
    public void recordEvent(final MonitoringEvent event) {
        final MonitoringFilterChain chain = new DefaultFilterChain(filters.iterator(), this);
        chain.filterEvent(event);
    }

    @Override
    public void recordMetric(final String name, final float value) {
        final MonitoringFilterChain chain = new DefaultFilterChain(filters.iterator(), this);
        chain.filterMetric(name, value);
    }

    @Override
    public void incrementCounter(final String name, final int incrementValue) {
        final MonitoringFilterChain chain = new DefaultFilterChain(filters.iterator(), this);
        chain.filterCounter(name, incrementValue);
    }

    protected void bindFilter(final MonitoringFilter filter, final Map<String, Object> properties) {
        final int ranking = PropertiesUtil.toInteger(properties.get(Constants.SERVICE_RANKING), 0);
        final PriorityFilter priorityFilter = new PriorityFilter(ranking, filter);
        filters.add(priorityFilter);
    }

    protected void unbindFilter(final MonitoringFilter filter) {
        final Iterator<PriorityFilter> filterIterator = filters.iterator();
        while (filterIterator.hasNext()) {
            final PriorityFilter priorityFilter = filterIterator.next();
            if (priorityFilter.wrapped.equals(filter)) {
                filterIterator.remove();
                return;
            }
        }
    }

    protected void bindWriter(final MonitoringWriter writer) {
        writers.add(writer);
    }

    protected void unbindWriter(final MonitoringWriter writer) {
        writers.remove(writer);
    }

    private void writeEvent(final MonitoringEvent event) {
        writers.forEach(writer -> writer.writeEvent(event));
    }

    private void writeMetric(final String name, final float value) {
        writers.forEach(writer -> writer.writeMetric(new DefaultMonitoringMetric(name, value)));
    }

    private void writeCounter(final String name, final int increment) {
        writers.forEach(writer -> writer.incrementCounter(new DefaultMonitoringCounter(name, increment)));
    }

    @Override
    public void filterEvent(final MonitoringEvent event) {
        writeEvent(event);
    }

    @Override
    public void filterMetric(final String name, final float value) {
        writeMetric(name, value);
    }

    @Override
    public void filterCounter(final String name, final int value) {
        writeCounter(name, value);
    }

    private class PriorityFilter implements MonitoringFilter, Comparable<PriorityFilter> {

        private final int priority;
        private final MonitoringFilter wrapped;

        private PriorityFilter(final int priority, final MonitoringFilter wrapped) {
            this.priority = priority;
            this.wrapped = wrapped;
        }

        @Override
        public void filterEvent(final MonitoringEvent event, final MonitoringFilterChain filterChain) {
            wrapped.filterEvent(event, filterChain);
        }

        @Override
        public void filterMetric(final String name, final float value, final MonitoringFilterChain filterChain) {
            wrapped.filterMetric(name, value, filterChain);
        }

        @Override
        public void filterCounter(final String name, final int value, final MonitoringFilterChain filterChain) {
            wrapped.filterCounter(name, value, filterChain);
        }

        @Override
        public int compareTo(final PriorityFilter o) {
            return o.priority - priority;
        }
    }

}
