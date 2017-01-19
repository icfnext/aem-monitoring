package com.icfolson.aem.monitoring.core.filter;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

import java.util.Iterator;

public class DefaultFilterChain implements MonitoringFilterChain {

    private final Iterator<? extends MonitoringFilter> iterator;
    private final MonitoringFilterChain last;

    public DefaultFilterChain(final Iterator<? extends MonitoringFilter> iterator, final MonitoringFilterChain last) {
        this.iterator = iterator;
        this.last = last;
    }

    @Override
    public void filterEvent(final MonitoringEvent event) {
        if (iterator.hasNext()) {
            iterator.next().filterEvent(event, this);
        } else {
            last.filterEvent(event);
        }
    }

    @Override
    public void filterMetric(final QualifiedName name, final float value) {
        if (iterator.hasNext()) {
            iterator.next().filterMetric(name, value, this);
        } else {
            last.filterMetric(name, value);
        }
    }

    @Override
    public void filterCounter(final QualifiedName name, final int value) {
        if (iterator.hasNext()) {
            iterator.next().filterCounter(name, value, this);
        } else {
            last.filterCounter(name, value);
        }
    }
}