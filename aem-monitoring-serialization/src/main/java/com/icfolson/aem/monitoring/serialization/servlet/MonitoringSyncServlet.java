package com.icfolson.aem.monitoring.serialization.servlet;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringMetric;
import com.icfolson.aem.monitoring.database.repository.CounterRepository;
import com.icfolson.aem.monitoring.database.repository.EventRepository;
import com.icfolson.aem.monitoring.database.repository.MetricRepository;
import com.icfolson.aem.monitoring.serialization.constants.Parameters;
import com.icfolson.aem.monitoring.serialization.constants.Paths;
import com.icfolson.aem.monitoring.serialization.model.CountersTable;
import com.icfolson.aem.monitoring.serialization.model.EventsTable;
import com.icfolson.aem.monitoring.serialization.model.MetricsTable;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

@SlingServlet(paths = Paths.ALL_SERVLET_PATH)
public class MonitoringSyncServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringSyncServlet.class);

    private static final long SINCE_DEFAULT = 0;
    private static final int LIMIT_DEFAULT = 1000;

    @Reference
    private EventRepository eventRepository;
    @Reference
    private MetricRepository metricRepository;
    @Reference
    private CounterRepository counterRepository;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException,
            IOException {

        final String eventsSinceString = request.getParameter(Parameters.EVENTS_SINCE);
        final String eventsLimitString = request.getParameter(Parameters.EVENTS_LIMIT);
        final String metricsSinceString = request.getParameter(Parameters.METRICS_SINCE);
        final String metricsLimitString = request.getParameter(Parameters.METRICS_LIMIT);
        final String countersSinceString = request.getParameter(Parameters.COUNTERS_SINCE);
        final String countersLimitString = request.getParameter(Parameters.COUNTERS_LIMIT);
        try {
            Long eventsSince = NumberUtils.createLong(eventsSinceString);
            Integer eventsLimit = NumberUtils.createInteger(eventsLimitString);
            Long metricsSince = NumberUtils.createLong(metricsSinceString);
            Integer metricsLimit = NumberUtils.createInteger(metricsLimitString);
            Long countersSince = NumberUtils.createLong(countersSinceString);
            Integer countersLimit = NumberUtils.createInteger(countersLimitString);
            if (eventsSince == null || eventsSince < 0) {
                eventsSince = SINCE_DEFAULT;
            }
            if (eventsLimit == null || eventsLimit < 0) {
                eventsLimit = LIMIT_DEFAULT;
            }
            if (metricsSince == null || metricsSince < 0) {
                metricsSince = SINCE_DEFAULT;
            }
            if (metricsLimit == null || metricsLimit < 0) {
                metricsLimit = LIMIT_DEFAULT;
            }
            if (countersSince == null || countersSince < 0) {
                countersSince = SINCE_DEFAULT;
            }
            if (countersLimit == null || countersLimit < 0) {
                countersLimit = LIMIT_DEFAULT;
            }
            final DataOutputStream outputStream = new DataOutputStream(response.getOutputStream());
            sendEvents(eventsSince, eventsLimit, outputStream);
            sendMetrics(metricsSince, metricsLimit, outputStream);
            sendCounters(countersSince, countersLimit, outputStream);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Both 'since' and 'limit' parameters must be "
                    + "valid long integer values.");
        }
    }

    private void sendEvents(final long since, final int limit, final DataOutputStream outputStream) {
        final List<MonitoringEvent> eventList = eventRepository.getEvents(since, limit);
        final EventsTable table = new EventsTable(eventList);
        table.writeTable(outputStream);
    }

    private void sendMetrics(final long since, final int limit, final DataOutputStream outputStream) {
        final List<MonitoringMetric> metricList = metricRepository.getMetrics(since, limit);
        final MetricsTable table = new MetricsTable(metricList);
        table.writeMetrics(outputStream);
    }

    private void sendCounters(final long since, final int limit, final DataOutputStream outputStream) {
        final List<MonitoringCounter> counterList = counterRepository.getCounters(since, limit);
        final CountersTable table = new CountersTable(counterList);
        table.writeCounters(outputStream);
    }

}
