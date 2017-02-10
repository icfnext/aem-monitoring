package com.icfolson.aem.monitoring.core.builtin;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.service.MonitoringService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Component(immediate = true)
@Property(name = ResourceChangeListener.PATHS, value = {"/content", "/etc"})
public class ResourceChangeRecorder implements ResourceChangeListener {

    @Reference
    private MonitoringService monitoringService;

    @Override
    public void onChange(List<ResourceChange> list) {
        for (ResourceChange change : list) {
            monitoringService.recordEvent(new ChangeEvent(change));
        }
    }

    private static class ChangeEvent implements MonitoringEvent {

        private static final String PATH = "path";
        private static final String TYPE = "type";
        private static final String USER = "user";

        private static final QualifiedName NAME = new QualifiedName("resource", "change");

        private final long timestamp = System.currentTimeMillis();
        private final Map<String, Object> value = new HashMap<>();

        private ChangeEvent(ResourceChange change) {
            value.put(PATH, change.getPath());
            value.put(TYPE, change.getType().name());
            value.put(USER, change.getUserId());
        }

        @Override
        public QualifiedName getType() {
            return NAME;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public void setProperty(String name, Object value) {

        }

        @Override
        public Map<String, Object> getProperties() {
            return value;
        }
    }

}
