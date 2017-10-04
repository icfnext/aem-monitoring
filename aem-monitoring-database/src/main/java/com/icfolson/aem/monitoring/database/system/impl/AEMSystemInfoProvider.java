package com.icfolson.aem.monitoring.database.system.impl;

import com.google.common.base.Joiner;
import com.icfolson.aem.monitoring.database.system.SystemInfo;
import com.icfolson.aem.monitoring.database.system.SystemInfoProvider;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.discovery.DiscoveryService;
import org.apache.sling.settings.SlingSettingsService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Component(immediate = true)
public class AEMSystemInfoProvider implements SystemInfoProvider {

    private static final String RUNMODES_PROP = "runmodes";
    private static final String IS_AUTHOR_PROP = "author";
    private static final String IS_PUBLISH_PROP = "publish";

    // TODO get machine name, etc

    @Reference
    private DiscoveryService discoveryService;
    
    @Reference
    private SlingSettingsService settingsService;

    private SystemInfo systemInfo;

    @Override
    public SystemInfo getSystemInfo() {
        return systemInfo;
    }
    
    @Activate
    private final void activate() {
        final UUID systemId = UUID.fromString(discoveryService.getTopology().getLocalInstance().getSlingId());
        final Map<String, String> properties = new HashMap<>();
        final Set<String> runmodes = settingsService.getRunModes();
        properties.put(RUNMODES_PROP, Joiner.on(',').join(runmodes));
        properties.put(IS_AUTHOR_PROP, Boolean.toString(runmodes.contains("author")));
        properties.put(IS_PUBLISH_PROP, Boolean.toString(runmodes.contains("publish")));
        systemInfo = new SystemInfoImpl(systemId, properties);
    }

    @Deactivate
    protected final void deactivate() {
        systemInfo = null;
    }

    @Modified
    protected final void modified() {
        deactivate();
        activate();
    }

    private class SystemInfoImpl implements SystemInfo {

        private final UUID systemId;
        private final Map<String, String> properties;

        private SystemInfoImpl(final UUID systemId, final Map<String, String> properties) {
            this.systemId = systemId;
            this.properties = properties;
        }

        @Override
        public UUID getSystemId() {
            return systemId;
        }

        @Override
        public Set<String> getPropertyNames() {
            return new HashSet<>(properties.keySet());
        }

        @Override
        public String getPropertyValue(final String propertyName) {
            return properties.get(propertyName);
        }
    }
}
