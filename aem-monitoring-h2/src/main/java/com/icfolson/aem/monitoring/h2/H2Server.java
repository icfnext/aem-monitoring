package com.icfolson.aem.monitoring.h2;

import com.icfolson.aem.monitoring.database.server.DatabaseServer;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.settings.SlingSettingsService;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Component(immediate = true, metatype = true, label = "AEM Monitoring: H2 Server", description =
        "Starts an H2 server, allowing remote servers to store data.")
public class H2Server implements DatabaseServer {

    private static final Logger LOG = LoggerFactory.getLogger(H2Server.class);

    private static final String URL_TEMPLATE = "jdbc:h2:tcp://%s:%s";

    private static final String SERVER_DEFAULT = "localhost";

    private static final int AUTHOR_PORT_DEFAULT = 8084;

    private static final int PUBLISH_PORT_DEFAULT = 8085;

    private static final String DB_REL_PATH = "/db";

    @Property(label = "Server", value = SERVER_DEFAULT)
    private static final String SERVER_PROP = "server";

    @Property(label = "Server Port")
    private static final String PORT_PROP = "port";

    @Property(label = "H2 Basedir", description = "The base directory for the H2 server.  Relative URLs will be "
            + "evaluated relative to the SLING_HOME directory. Keep blank to use sling home.")
    private static final String BASEDIR_PROP = "basedir";

    @Property(label = "Allow Remote", boolValue = true)
    private static final String ALLOW_REMOTE_PROP = "allow.remote";

    @Property(label = "Externally Managed", boolValue = false)
    private static final String EXTERNALLY_MANAGED_PROP = "externally.managed";

    @Reference
    private SlingSettingsService settingsService;

    private Server server;
    private String url;

    @Override
    public String getConnectionURL() {
        return url;
    }

    @Activate
    protected void activate(final Map<String, Object> props) {
        final int portDefault = settingsService.getRunModes().contains("author")
                ? AUTHOR_PORT_DEFAULT : PUBLISH_PORT_DEFAULT;
        final String serverName = PropertiesUtil.toString(props.get(SERVER_PROP), SERVER_DEFAULT);
        final int port = PropertiesUtil.toInteger(props.get(PORT_PROP), portDefault);
        final String basedir = PropertiesUtil.toString(props.get(BASEDIR_PROP), settingsService.getSlingHomePath()
                + DB_REL_PATH);
        final boolean allowRemote = PropertiesUtil.toBoolean(props.get(ALLOW_REMOTE_PROP), false);
        final boolean external = PropertiesUtil.toBoolean(props.get(EXTERNALLY_MANAGED_PROP), false);
        final String finalBasedir = basedir.startsWith("/") ? basedir
                : settingsService.getSlingHomePath() + DB_REL_PATH + "/" + basedir;
        url = String.format(URL_TEMPLATE, serverName, port);
        final List<String> args = new ArrayList<>();
        if (!external) {
            args.add("-tcpPort");
            args.add(Integer.toString(port));
            if (allowRemote) {
                args.add("-tcpAllowOthers");
            }
            if (!finalBasedir.isEmpty()) {
                args.add("-baseDir");
                args.add(finalBasedir);
            }
            try {
                server = Server.createTcpServer(args.toArray(new String[args.size()])).start();
            } catch (SQLException e) {
                LOG.error("Error starting H2 server", e);
            }
        }
    }

    @Deactivate
    protected void deactivate() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Modified
    protected void modified(final Map<String, Object> props) {
        deactivate();
        activate(props);
    }
}
