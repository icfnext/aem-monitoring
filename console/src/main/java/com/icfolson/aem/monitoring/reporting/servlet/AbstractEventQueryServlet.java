package com.icfolson.aem.monitoring.reporting.servlet;

import com.icfolson.aem.monitoring.reporting.model.EventQuery;
import com.icfolson.aem.monitoring.reporting.model.Operation;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;

public class AbstractEventQueryServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractEventQueryServlet.class);

    private static final String PARAM_TYPE = "type";
    private static final String PARAM_FACET = "facet";
    private static final String PARAM_Y_AXIS = "y-axis";
    private static final String PARAM_START_EPOCH = "start-epoch";
    private static final String PARAM_END_EPOCH = "end-epoch";
    private static final String PARAM_FILTER = "filter";
    private static final Short DEFAULT_TYPE = 1;

    protected final EventQuery parse(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
        throws IOException {

        final String typeString = request.getParameter(PARAM_TYPE);
        final String facet = request.getParameter(PARAM_FACET);
        final String yAxis = request.getParameter(PARAM_Y_AXIS);
        final String startEpoch = request.getParameter(PARAM_START_EPOCH);
        final String endEpoch = request.getParameter(PARAM_END_EPOCH);
        final String[] filters = request.getParameterValues(PARAM_FILTER);

        final EventQuery query = new EventQuery();

        try {
            query.setEventType(Short.parseShort(typeString));
        } catch (NumberFormatException e) {
            query.setEventType(DEFAULT_TYPE);
        }

        query.setBinCount(60);
        final long now = System.currentTimeMillis();
        query.setWindowEnd(now);
        query.setWindowStart(now - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS));
        query.setFacetProperty(facet);
        query.setMetricProperty(yAxis);
        if (filters != null) {
            for (final String filter : filters) {
                final String decoded = URLDecoder.decode(filter);
                String[] split = decoded.split(" ");
                if (split.length == 3) {
                    Operation operation = Operation.fromString(split[1]);
                    if (operation != null) {
                        query.getPredicates().add(operation.predicate(split[0].trim(), split[2].trim()));
                    }
                }
            }
        }
        if (startEpoch != null) {
            try {
                query.setWindowStart(Long.parseLong(startEpoch));
            } catch (NumberFormatException e) {
                LOG.error("Invalid start epoch in request: " + startEpoch, e);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }
        }
        if (endEpoch != null) {
            try {
                query.setWindowEnd(Long.parseLong(endEpoch));
            } catch (NumberFormatException e) {
                LOG.error("Invalid end epoch in request: " + endEpoch, e);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }
        }
        return query;
    }

}
