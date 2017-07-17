package com.icfolson.aem.monitoring.console.servlet;

import com.day.text.csv.Csv;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icfolson.aem.monitoring.console.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.console.model.EventQuery;
import com.icfolson.aem.monitoring.console.result.EventListing;
import com.icfolson.aem.monitoring.console.service.EventQueryService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.http.entity.ContentType;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SlingServlet(paths = "/bin/monitoring/eventData", extensions = {"json", "csv"}, methods = "GET")
public class EventServlet extends AbstractEventQueryServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Reference
    private EventQueryService service;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
        throws ServletException, IOException {

        if ("csv".equalsIgnoreCase(request.getRequestPathInfo().getExtension())) {
            serveCsv(request, response);
        } else {
            serveJson(request, response);
        }
    }

    private void serveCsv(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {

        final EventQuery query = parse(request, response);
        try {
            if (query != null) {
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "inline");
                Csv csv = new Csv();
                csv.writeInit(response.getWriter());
                final EventListing result = service.getEvents(query);
                List<String> columns = new ArrayList<>();
                columns.addAll(result.getPropertyNames());
                String[] values = new String[columns.size()];
                columns.toArray(values);
                csv.writeRow(values);
                for (final Map<String, Object> event : result.getEvents()) {
                    for (int i = 0; i < columns.size(); i++) {
                        final Object value = event.get(columns.get(i));
                        values[i] = value instanceof String ? (String) value : null;
                    }
                    csv.writeRow(values);
                }
                csv.close();
            }
        } catch (MonitoringQueryException e) {
            response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void serveJson(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {

        final EventQuery query = parse(request, response);
        try {
            if (query != null) {
                final EventListing result = service.getEvents(query);
                response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                MAPPER.writeValue(response.getOutputStream(), result);
            }
        } catch (MonitoringQueryException e) {
            response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
