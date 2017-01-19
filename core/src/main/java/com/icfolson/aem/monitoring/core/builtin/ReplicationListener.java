package com.icfolson.aem.monitoring.core.builtin;

import com.day.cq.replication.ReplicationAction;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.model.impl.DefaultMonitoringEvent;
import com.icfolson.aem.monitoring.core.service.MonitoringService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

@Service
@Component(immediate=true)
@Properties({
    @Property(name = EventConstants.EVENT_TOPIC, value = ReplicationAction.EVENT_TOPIC)
})
public class ReplicationListener implements EventHandler {

    @Reference
    private MonitoringService service;

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
}
