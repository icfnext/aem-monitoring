package com.icfolson.aem.monitoring.core.builtin;

import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.service.MonitoringService;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.*;

@Service
@Component(immediate = true, metatype = true, label = "AEM Monitoring: Resource change recorder")
public class ResourceChangeRecorder implements ResourceChangeListener {

    private static final String PID_PROP = "service.pid";

    @Property(label = "Disable", boolValue = false, description = "Check to disable resource change monitoring events")
    private static final String DISABLE_PROP = "disable";

    @Property(label = "Watched Paths", value = {"/content", "/etc"})
    private static final String WATCHED_PATHS = "watched.paths";

    @Reference
    private MonitoringService monitoringService;

    @Reference
    private ConfigurationAdmin configAdmin;

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

    @Activate
    @Modified
    protected final void modified(final Map<String, Object> props) throws IOException {
        final String pid = PropertiesUtil.toString(props.get(PID_PROP), null);
        final boolean disabled = PropertiesUtil.toBoolean(props.get(DISABLE_PROP), false);
        final boolean isListener = props.containsKey(ResourceChangeListener.PATHS);
        if (disabled == isListener) {
            final Configuration configuration = configAdmin.getConfiguration(pid);
            final Dictionary<String, Object> dictionary = new Hashtable<>(props);
            if (disabled) {
                dictionary.remove(ResourceChangeListener.PATHS);
            } else {
                final String[] watchedPaths = PropertiesUtil.toStringArray(props.get(WATCHED_PATHS), null);
                dictionary.put(ResourceChangeListener.PATHS, watchedPaths);
            }
            configuration.update(dictionary);
        }
    }

}
