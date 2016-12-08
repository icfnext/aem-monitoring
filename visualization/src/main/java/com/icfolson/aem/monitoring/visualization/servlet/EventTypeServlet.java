package com.icfolson.aem.monitoring.visualization.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icfolson.aem.monitoring.visualization.exception.MonitoringQueryException;
import com.icfolson.aem.monitoring.visualization.model.EventTypeDescriptor;
import com.icfolson.aem.monitoring.visualization.service.EventQueryService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.http.entity.ContentType;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@SlingServlet(paths = "/bin/monitoring/eventTypes", extensions = "json")
public class EventTypeServlet extends SlingAllMethodsServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Reference
    private EventQueryService service;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
        throws ServletException, IOException {
        try {
            final List<EventTypeDescriptor> result = service.getEventDescriptors();
            response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            MAPPER.writeValue(response.getOutputStream(), result);
        } catch (MonitoringQueryException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
