package com.icfolson.aem.monitoring.core.builtin;

import com.icfolson.aem.monitoring.core.constants.EventTypes;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.service.MonitoringService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.wrappers.SlingHttpServletResponseWrapper;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SlingFilter(order = Integer.MIN_VALUE, scope = SlingFilterScope.REQUEST, metatype = true,
    label = "AEM Monitoring: Sling Request Recorder")
public class SlingRequestRecorder implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(SlingRequestRecorder.class);

    @Property(label = "Captured Headers", value = {"Referer", "Host"})
    private static final String CAPTURE_HEADERS_PROP = "capture.headers";

    @Property(label = "Disable", boolValue = false, description = "Check to disable sling request events")
    private static final String DISABLE_PROP = "disable";

    private static final QualifiedName SLING_REQUEST = EventTypes.SLING.getChild("request");

    @Reference
    private MonitoringService service;

    @Reference
    private ConfigurationAdmin configAdmin;

    private final Set<String> capturedHeaders = new HashSet<>();

    private boolean disabled;

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
        final FilterChain filterChain) throws IOException, ServletException {

        if (disabled) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        service.initializeTransaction(SLING_REQUEST);
        if (servletRequest instanceof SlingHttpServletRequest) {
            final SlingHttpServletRequest request = (SlingHttpServletRequest) servletRequest;
            service.setTransactionProperty("request.path", request.getRequestPathInfo().getResourcePath());
            service.setTransactionProperty("request.selectors", request.getRequestPathInfo().getSelectorString());
            service.setTransactionProperty("request.extension", request.getRequestPathInfo().getExtension());
            service.setTransactionProperty("request.suffix", request.getRequestPathInfo().getSuffix());
            service.setTransactionProperty("request.method", request.getMethod());
            for (final String headerName : capturedHeaders) {
                final String headerValue = request.getHeader(headerName);
                if (headerValue != null) {
                    final String propertyName = "request.header." + headerName.toLowerCase();
                    service.setTransactionProperty(propertyName, headerValue);
                }
            }
            service.setTransactionProperty("request.user", request.getResourceResolver().getUserID());
        }

        ServletResponse response = servletResponse;
        if (servletResponse instanceof SlingHttpServletResponse) {
            response = new ResponseWrapper((SlingHttpServletResponse) servletResponse);
        }

        filterChain.doFilter(servletRequest, response);

        if (response instanceof ResponseWrapper) {
            final ResponseWrapper responseWrapper = (ResponseWrapper) response;
            service.setTransactionProperty("response.length", responseWrapper.getResponseLength());
            service.setTransactionProperty("response.status", String.valueOf(responseWrapper.getStatus()));
        }
        service.recordTransaction();
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException { }

    @Override
    public void destroy() { }

    @Activate
    @Modified
    protected void modified(final Map<String, Object> props) {
        capturedHeaders.clear();
        final String[] headers = PropertiesUtil.toStringArray(props.get(CAPTURE_HEADERS_PROP));
        capturedHeaders.addAll(Arrays.asList(headers));
        disabled = PropertiesUtil.toBoolean(props.get(DISABLE_PROP), false);
    }

    private class ResponseWrapper extends SlingHttpServletResponseWrapper {

        public ResponseWrapper(final SlingHttpServletResponse wrappedResponse) {
            super(wrappedResponse);
        }

        public int getResponseLength() {
            // TODO fix this
            try {
                final PrintWriter printWriter = getResponse().getWriter();
                final Class c = printWriter.getClass();
                final Field count = c.getDeclaredField("count");
                count.setAccessible(true);
                return count.getInt(printWriter);
            } catch (Exception e) {
                LOG.trace("Error getting length", e);
            }
            try {
                final OutputStream outputStream = getResponse().getOutputStream();
                final Class c = outputStream.getClass();
                final Field count = c.getDeclaredField("count");
                count.setAccessible(true);
                return count.getInt(outputStream);
            } catch (Exception e) {
                LOG.trace("Error getting length", e);
            }
            return 0;
        }
    }
}
