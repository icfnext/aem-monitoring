package com.icfolson.aem.monitoring.serialization.servlet;

import com.icfolson.aem.monitoring.database.system.SystemInfo;
import com.icfolson.aem.monitoring.database.system.SystemInfoProvider;
import com.icfolson.aem.monitoring.serialization.constants.Paths;
import com.icfolson.aem.monitoring.serialization.model.SystemTable;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import javax.servlet.ServletException;
import java.io.IOException;

@SlingServlet(paths = Paths.SYSTEM_SERVLET_PATH)
public class SystemServlet extends SlingAllMethodsServlet {

    @Reference
    private SystemInfoProvider systemInfoProvider;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
        throws ServletException, IOException {

        final SystemInfo info = systemInfoProvider.getSystemInfo();
        final SystemTable table = new SystemTable();
        table.setUuid(info.getSystemId());
        for (final String s : info.getPropertyNames()) {
            table.getProperties().put(s, info.getPropertyValue(s));
        }
        table.writeTable(response.getOutputStream());
    }
}
