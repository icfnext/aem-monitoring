package com.icfolson.aem.monitoring.reporting.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icfolson.aem.monitoring.core.util.NameUtil;
import com.icfolson.aem.monitoring.database.repository.CounterRepository;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.http.entity.ContentType;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SlingServlet(paths = "/bin/monitoring/counterTypes", extensions = "json")
public class CounterTypesServlet extends SlingAllMethodsServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final char JOIN = '/';

    @Reference
    private CounterRepository repository;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
        throws ServletException, IOException {
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        final Map<String, Short> metricTypes = repository.getCounterTypes();
        final Map<String, Short> namedTypes = new HashMap<>();
        metricTypes.entrySet().forEach(e -> namedTypes.put(NameUtil.toName(e.getKey()).getJoined(JOIN), e.getValue()));
        MAPPER.writeValue(response.getOutputStream(), namedTypes);
    }
}
