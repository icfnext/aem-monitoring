package com.icfolson.aem.monitoring.h2;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.settings.SlingSettingsService;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(immediate = true, metatype = true, policy = ConfigurationPolicy.REQUIRE, label = "H2 Server", description =
    "Starts an H2 server, allowing remote servers to store data.")
public class H2Server {

    private static final Logger LOG = LoggerFactory.getLogger(H2Server.class);

    private static final int PORT_DEFAULT = 8025;

    @Property(label = "Server Port", intValue = PORT_DEFAULT)
    private static final String PORT_PROP = "port";

    @Property(label = "H2 Basedir", description = "The base directory for the H2 server.  Relative URLs will be "
        + "evaluated relative to the SLING_HOME directory.  Leave blank to not use the basedir arg (not recommended).")
    private static final String BASEDIR_PROP = "basedir";

    @Property(label = "Allow Remote", boolValue = true)
    private static final String ALLOW_REMOTE_PROP = "allow.remote";

    @Reference
    private SlingSettingsService settingsService;

    private Server server;

    @Activate
    protected void activate(final Map<String, Object> props) {
        final int port = PropertiesUtil.toInteger(props.get(PORT_PROP), PORT_DEFAULT);
        final String basedir = PropertiesUtil.toString(props.get(BASEDIR_PROP), "");
        final boolean allowRemote = PropertiesUtil.toBoolean(props.get(ALLOW_REMOTE_PROP), false);
        final String finalBasedir = basedir.startsWith("/") || basedir.isEmpty() ? basedir
            : settingsService.getSlingHomePath() + basedir;
        final List<String> args = new ArrayList<>();
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
