package com.icfolson.aem.monitoring.serialization.servlet;

import com.icfolson.aem.monitoring.core.model.MonitoringCounter;
import com.icfolson.aem.monitoring.database.repository.CounterRepository;
import com.icfolson.aem.monitoring.serialization.constants.Parameters;
import com.icfolson.aem.monitoring.serialization.constants.Paths;
import com.icfolson.aem.monitoring.serialization.model.CountersTable;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@SlingServlet(paths = Paths.COUNTERS_SERVLET_PATH)
public class CountersServlet extends SlingAllMethodsServlet {

    private static final long SINCE_DEFAULT = 0;
    private static final int LIMIT_DEFAULT = 1000;

    @Reference
    private CounterRepository repository;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
        throws ServletException, IOException {

        final String sinceString = request.getParameter(Parameters.SINCE);
        final String limitString = request.getParameter(Parameters.LIMIT);
        try {
            Long since = NumberUtils.createLong(sinceString);
            Integer limit = NumberUtils.createInteger(limitString);
            if (since == null || since < 0) {
                since = SINCE_DEFAULT;
            }
            if (limit == null || limit < 0) {
                limit = LIMIT_DEFAULT;
            }
            final List<MonitoringCounter> counterList = repository.getCounters(since, limit);
            final CountersTable table = new CountersTable(counterList);
            table.writeCounters(response.getOutputStream());
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Both 'since' and 'limit' parameters must be "
                + "valid long integer values.");
        }
    }
}
