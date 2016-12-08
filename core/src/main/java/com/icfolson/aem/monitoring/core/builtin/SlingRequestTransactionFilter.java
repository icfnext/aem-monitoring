package com.icfolson.aem.monitoring.core.builtin;

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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SlingFilter(order = Integer.MIN_VALUE, scope = SlingFilterScope.REQUEST, metatype = true,
    label = "AEM Monitoring: Sling Request Filter")
public class SlingRequestTransactionFilter implements Filter {

    @Property(label = "Captured Headers", value = {"Referer", "Host"})
    private static final String CAPTURE_HEADERS_PROP = "capture.headers";

    @Reference
    private MonitoringService service;

    private final Set<String> capturedHeaders = new HashSet<>();

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
        final FilterChain filterChain) throws IOException, ServletException {

        service.initializeTransaction("Sling Request");
        if (servletRequest instanceof SlingHttpServletRequest) {
            final SlingHttpServletRequest request = (SlingHttpServletRequest) servletRequest;
            service.setTransactionProperty("sling.path", request.getRequestPathInfo().getResourcePath());
            service.setTransactionProperty("sling.selectors", request.getRequestPathInfo().getSelectorString());
            service.setTransactionProperty("sling.extension", request.getRequestPathInfo().getExtension());
            service.setTransactionProperty("sling.suffix", request.getRequestPathInfo().getSuffix());
            service.setTransactionProperty("sling.method", request.getMethod());
            for (final String headerName : capturedHeaders) {
                final String headerValue = request.getHeader(headerName);
                if (headerValue != null) {
                    final String propertyName = "sling.header." + headerName.toLowerCase();
                    service.setTransactionProperty(propertyName, headerValue);
                }
            }
            service.setTransactionProperty("sling.user", request.getResourceResolver().getUserID());
        }

        ServletResponse response = servletResponse;
        if (servletResponse instanceof SlingHttpServletResponse) {
            response = new ResponseWrapper((SlingHttpServletResponse) servletResponse);
        }

        filterChain.doFilter(servletRequest, response);

        if (response instanceof ResponseWrapper) {
            service.setTransactionProperty("sling.status", String.valueOf(((ResponseWrapper) response).getStatus()));
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
        String[] headers = PropertiesUtil.toStringArray(props.get(CAPTURE_HEADERS_PROP));
        capturedHeaders.addAll(Arrays.asList(headers));
    }

    private class ResponseWrapper extends SlingHttpServletResponseWrapper {

        public ResponseWrapper(final SlingHttpServletResponse wrappedResponse) {
            super(wrappedResponse);
        }

    }
}
