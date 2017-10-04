package com.icfolson.aem.monitoring.serialization.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icfolson.aem.monitoring.core.model.RemoteSystem;
import com.icfolson.aem.monitoring.serialization.manager.ClientDataRepository;
import com.icfolson.aem.monitoring.serialization.manager.ClientManager;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.http.entity.ContentType;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SlingServlet(paths = "/bin/monitoring/clients", extensions = "json", methods = {"GET", "POST", "DELETE"})
public class ClientManagementServlet extends SlingAllMethodsServlet {

    private static final String DELETE_PARAM = "delete";
    private static final String NAME_PARAM = "name";
    private static final String HOST_PARAM = "host";
    private static final String PORT_PARAM = "port";
    private static final String USER_PARAM = "user";
    private static final String PASSWORD_PARAM = "password";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Reference
    private ClientDataRepository repository;

    @Reference
    private ClientManager manager;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException,
            IOException {

        final Map<String, RemoteSystem> systems = repository.getConfiguredSystems();
        systems.values().forEach(system -> system.setPassword(PASSWORD_PARAM)); // don't send actual values
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        MAPPER.writeValue(response.getOutputStream(), systems);
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException,
            IOException {

        final String delete = request.getParameter(DELETE_PARAM);
        final String name = request.getParameter(NAME_PARAM);
        final String host = request.getParameter(HOST_PARAM);
        final String port = request.getParameter(PORT_PARAM);
        final String user = request.getParameter(USER_PARAM);
        final String password = request.getParameter(PASSWORD_PARAM);

        if (Boolean.TRUE.toString().equalsIgnoreCase(delete) && name != null) {
            doDelete(name);
            return;
        }

        final List<String> errors = new ArrayList<>();
        if (host == null || host.trim().isEmpty()) {
            errors.add("'host' must not be empty");
        }
        if (port == null || port.isEmpty() || !NumberUtils.isDigits(port)) {
            errors.add("'port' must be a positive integer value");
        }
        if (user == null || user.isEmpty()) {
            errors.add("'user' must not be empty");
        }
        if (password == null || password.isEmpty()) {
            errors.add("'password' must not be empty");
        }

        if (!errors.isEmpty()) {
            response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            MAPPER.writeValue(response.getOutputStream(), errors);
        } else {
            final Integer portNumber = NumberUtils.createInteger(port);
            final RemoteSystem remoteSystem = new RemoteSystem();
            remoteSystem.setHost(host);
            remoteSystem.setPort(portNumber);
            remoteSystem.setUser(user);
            remoteSystem.setPassword(password);
            repository.setConfiguredSystem(name == null || name.isEmpty() ? null : name, remoteSystem);
            manager.restartClients();
        }
    }

    protected void doDelete(final String name) {
        repository.setConfiguredSystem(name, null);
    }
}
