package com.icfolson.aem.monitoring.core.builtin;

import com.day.cq.replication.ReplicationAction;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.model.base.DefaultMonitoringEvent;
import com.icfolson.aem.monitoring.core.service.MonitoringService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

@Service
@Component(immediate = true, metatype = true, label = "AEM Monitoring: Replication Recorder")
public class ReplicationRecorder implements EventHandler {

    private static final String PID_PROP = "service.pid";

    @Property(label = "Disable", boolValue = false, description = "Check to disable AEM replication events")
    private static final String DISABLE_PROP = "disable";

    @Reference
    private MonitoringService service;

    @Reference
    private ConfigurationAdmin configAdmin;

    @Override
    public void handleEvent(final Event e) {
        ReplicationAction action = ReplicationAction.fromEvent(e);
        for (final String path : action.getPaths()) {
            DefaultMonitoringEvent event = new DefaultMonitoringEvent(new QualifiedName("aem", "replication"));
            event.setProperty("user", action.getUserId());
            event.setProperty("type", action.getType().getName());
            event.setProperty("path", path);
            service.recordEvent(event);
        }
    }

    @Activate
    @Modified
    protected final void activate(final Map<String, Object> props) throws IOException {
        final String pid = PropertiesUtil.toString(props.get(PID_PROP), null);
        final boolean disabled = PropertiesUtil.toBoolean(props.get(DISABLE_PROP), false);
        final boolean isListener = props.containsKey(EventConstants.EVENT_TOPIC);
        if (disabled == isListener) {
            final Configuration configuration = configAdmin.getConfiguration(pid);
            final Dictionary<String, Object> dictionary = new Hashtable<>(props);
            if (disabled) {
                dictionary.remove(EventConstants.EVENT_TOPIC);
            } else {
                dictionary.put(EventConstants.EVENT_TOPIC, ReplicationAction.EVENT_TOPIC);
            }
            configuration.update(dictionary);
        }
    }
}
