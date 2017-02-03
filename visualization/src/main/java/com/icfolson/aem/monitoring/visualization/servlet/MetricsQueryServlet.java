package com.icfolson.aem.monitoring.visualization.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icfolson.aem.monitoring.visualization.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.visualization.model.MetricsQuery;
import com.icfolson.aem.monitoring.visualization.result.MetricsTimeSeries;
import com.icfolson.aem.monitoring.visualization.service.MetricsQueryService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.http.entity.ContentType;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SlingServlet(paths = "/bin/monitoring/metrics", extensions = "json", methods = "GET")
public class MetricsQueryServlet extends SlingAllMethodsServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String PARAM_TYPE = "type";
    private static final String PARAM_START_EPOCH = "start-epoch";
    private static final String PARAM_END_EPOCH = "end-epoch";

    @Reference
    private MetricsQueryService service;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
        throws ServletException, IOException {

        final String typeString = request.getParameter(PARAM_TYPE);
        final String startEpochString = request.getParameter(PARAM_START_EPOCH);
        final String endEpochString = request.getParameter(PARAM_END_EPOCH);

        final MetricsQuery query = new MetricsQuery();

        try {
            query.setType(Short.parseShort(typeString));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, PARAM_TYPE + " must be a short integer");
        }

        try {
            query.setStartEpoch(Long.parseLong(startEpochString));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, PARAM_START_EPOCH + " must be a long integer");
        }

        try {
            query.setEndEpoch(Long.parseLong(endEpochString));
        } catch (NumberFormatException e) {
            query.setEndEpoch(System.currentTimeMillis());
        }

        try {
            final MetricsTimeSeries timeSeries = service.executeQuery(query);
            response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            MAPPER.writeValue(response.getWriter(), timeSeries);
        } catch (MonitoringQueryException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
}
