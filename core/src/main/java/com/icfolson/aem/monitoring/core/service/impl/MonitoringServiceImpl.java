package com.icfolson.aem.monitoring.core.service.impl;

import com.google.common.collect.Iterators;
import com.icfolson.aem.monitoring.core.constants.EventProperties;
import com.icfolson.aem.monitoring.core.filter.DefaultFilterChain;
import com.icfolson.aem.monitoring.core.filter.MonitoringFilter;
import com.icfolson.aem.monitoring.core.filter.MonitoringFilterChain;
import com.icfolson.aem.monitoring.core.model.*;
import com.icfolson.aem.monitoring.core.model.base.DefaultMonitoringTransaction;
import com.icfolson.aem.monitoring.core.service.MonitoringService;
import com.icfolson.aem.monitoring.core.writer.MonitoringWriter;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
@Component(immediate = true)
public class MonitoringServiceImpl implements MonitoringService {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringServiceImpl.class);

    @Reference(referenceInterface = MonitoringFilter.class, policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, bind = "bindFilter", unbind = "unbindFilter")
    private final PriorityQueue<PriorityFilter> inputFilters = new PriorityQueue<>();
    private final Map<String, PriorityQueue<PriorityFilter>> filterMap = new HashMap<>();

    @Reference(referenceInterface = MonitoringWriter.class, policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, bind = "bindWriter", unbind = "unbindWriter")
    private final Map<String, MonitoringWriter> writerMap = new HashMap<>();

    private final ThreadLocal<DefaultMonitoringTransaction> currentTransaction = new ThreadLocal<>();

    private final InputFilterTerminator inputFilterTerminator = new InputFilterTerminator();

    @Override
    public void initializeTransaction(final QualifiedName name) {
        if (currentTransaction.get() != null) {
            //throw new IllegalStateException("A transaction already exists for the current thread");
        }
        currentTransaction.set(new DefaultMonitoringTransaction(name));
    }

    @Override
    public QualifiedName getTransactionType() {
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
        final long startTime = transaction.getStartTime();
        final long endTime = transaction.getTimestamp();
        final long length = endTime - startTime;
        transaction.setProperty(EventProperties.TRANSACTION_LENGTH_MS, length);
        recordEvent(transaction);
    }

    @Override
    public void recordEvent(final MonitoringEvent event) {
        try {
            final MonitoringFilterChain chain = new DefaultFilterChain(inputFilters.iterator(), inputFilterTerminator);
            chain.filterEvent(event);
        } catch (Exception e) {
            LOG.error("Error recording event", e);
        }
    }

    @Override
    public void recordMetric(final QualifiedName name, final float value) {
        try {
            final MonitoringFilterChain chain = new DefaultFilterChain(inputFilters.iterator(), inputFilterTerminator);
            chain.filterMetric(name, value);
        } catch (Exception e) {
            LOG.error("Error recording metric", e);
        }
    }

    @Override
    public void incrementCounter(final QualifiedName name, final int incrementValue) {
        try {
            final MonitoringFilterChain chain = new DefaultFilterChain(inputFilters.iterator(), inputFilterTerminator);
            chain.filterCounter(name, incrementValue);
        } catch (Exception e) {
            LOG.error("Error recording counter", e);
        }
    }

    protected void bindFilter(final MonitoringFilter filter, final Map<String, Object> properties) {
        String context = PropertiesUtil.toString(properties.get(MonitoringFilter.CONTEXT_PROP), null);
        final PriorityQueue<PriorityFilter> filters;
        if (context == null) {
            filters = inputFilters;
        } else {
            filters = filterMap.computeIfAbsent(context, k -> new PriorityQueue<>());
        }
        final int ranking = PropertiesUtil.toInteger(properties.get(Constants.SERVICE_RANKING), 0);
        final PriorityFilter priorityFilter = new PriorityFilter(ranking, filter);
        filters.add(priorityFilter);
        LOG.info("Added filter {} to context {}", filter.getClass(), context);

    }

    protected void unbindFilter(final MonitoringFilter filter, final Map<String, Object> properties) {
        final String context = PropertiesUtil.toString(properties.get(MonitoringFilter.CONTEXT_PROP), null);
        final PriorityQueue<PriorityFilter> filters = filterMap.get(context);
        if (filters != null) {
            final Iterator<PriorityFilter> filterIterator = filters.iterator();
            while (filterIterator.hasNext()) {
                final PriorityFilter priorityFilter = filterIterator.next();
                if (priorityFilter.wrapped.equals(filter)) {
                    filterIterator.remove();
                    return;
                }
            }
        }
    }

    protected void bindWriter(final MonitoringWriter writer, final Map<String, Object> properties) {
        final String name = writer.getWriterName();
        final boolean disabled = PropertiesUtil.toBoolean(properties.get(MonitoringWriter.DISABLED_PROP), false);
        if (disabled) {
            LOG.info("MonitoringWriter disabled: {}", writer.getClass());
        } else {
            if (name == null) {
                LOG.warn("MonitoringWriter service {} does not specify a 'writer.name' property and will be ignored.",
                        writer.getClass());
            } else {
                writerMap.put(name, writer);
            }
        }
    }

    protected void unbindWriter(final MonitoringWriter writer) {
        writerMap.remove(writer.getWriterName());
    }

    private void writeEvent(final MonitoringEvent event) {
        for (Map.Entry<String, MonitoringWriter> e : writerMap.entrySet()) {
            final MonitoringFilterChain chain = constructWriterChain(e.getKey(), e.getValue());
            chain.filterEvent(event);
        }
    }

    private void writeMetric(final QualifiedName name, final float value) {
        for (Map.Entry<String, MonitoringWriter> e : writerMap.entrySet()) {
            final MonitoringFilterChain chain = constructWriterChain(e.getKey(), e.getValue());
            chain.filterMetric(name, value);
        }
    }

    private void writeCounter(final QualifiedName name, final int increment) {
        for (Map.Entry<String, MonitoringWriter> e : writerMap.entrySet()) {
            final MonitoringFilterChain chain = constructWriterChain(e.getKey(), e.getValue());
            chain.filterCounter(name, increment);
        }
    }

    private MonitoringFilterChain constructWriterChain(final String name, final MonitoringWriter writer) {
        final WriterFilterTerminator terminator = WriterFilterTerminator.getInstance(name, writer);
        final PriorityQueue<PriorityFilter> filters = filterMap.get(name);
        final Iterator<PriorityFilter> iterator = filters == null ? Iterators.emptyIterator() : filters.iterator();
        return new DefaultFilterChain(iterator, terminator);
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
        public void filterMetric(final QualifiedName name, final float value, final MonitoringFilterChain filterChain) {
            wrapped.filterMetric(name, value, filterChain);
        }

        @Override
        public void filterCounter(final QualifiedName name, final int value, final MonitoringFilterChain filterChain) {
            wrapped.filterCounter(name, value, filterChain);
        }

        @Override
        public int compareTo(final PriorityFilter o) {
            return o.priority - priority;
        }
    }

    private class InputFilterTerminator implements MonitoringFilterChain {

        @Override
        public void filterEvent(final MonitoringEvent event) {
            writeEvent(event);
        }

        @Override
        public void filterMetric(final QualifiedName name, final float value) {
            writeMetric(name, value);
        }

        @Override
        public void filterCounter(final QualifiedName name, final int value) {
            writeCounter(name, value);
        }
    }

    private static class WriterFilterTerminator implements MonitoringFilterChain {

        private static final Map<String, WriterFilterTerminator> INSTANCES = new HashMap<>();

        public static WriterFilterTerminator getInstance(final String name, final MonitoringWriter writer) {
            return INSTANCES.computeIfAbsent(name, k -> new WriterFilterTerminator(writer));
        }

        private final MonitoringWriter writer;

        private WriterFilterTerminator(MonitoringWriter writer) {
            this.writer = writer;
        }

        @Override
        public void filterEvent(MonitoringEvent event) {
            writer.writeEvent(event);
        }

        @Override
        public void filterMetric(QualifiedName name, float value) {
            final long timestamp = System.currentTimeMillis();
            writer.writeMetric(new MonitoringMetric() {
                @Override
                public QualifiedName getName() {
                    return name;
                }

                @Override
                public long getTimestamp() {
                    return timestamp;
                }

                @Override
                public float getValue() {
                    return value;
                }
            });
        }

        @Override
        public void filterCounter(QualifiedName name, int value) {
            final long timestamp = System.currentTimeMillis();
            writer.incrementCounter(new MonitoringCounter() {
                @Override
                public QualifiedName getName() {
                    return name;
                }

                @Override
                public long getTimestamp() {
                    return timestamp;
                }

                @Override
                public int getIncrement() {
                    return value;
                }
            });
        }
    }

}
