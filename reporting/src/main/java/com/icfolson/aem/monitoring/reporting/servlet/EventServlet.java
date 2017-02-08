package com.icfolson.aem.monitoring.reporting.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icfolson.aem.monitoring.reporting.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.reporting.model.EventQuery;
import com.icfolson.aem.monitoring.reporting.result.EventListing;
import com.icfolson.aem.monitoring.reporting.service.EventQueryService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.http.entity.ContentType;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;

@SlingServlet(paths = "/bin/monitoring/eventData", extensions = "json")
public class EventServlet extends AbstractEventQueryServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Reference
    private EventQueryService service;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
        throws ServletException, IOException {

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
