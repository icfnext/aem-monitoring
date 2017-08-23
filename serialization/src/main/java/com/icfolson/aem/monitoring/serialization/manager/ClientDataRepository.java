package com.icfolson.aem.monitoring.serialization.manager;

import com.google.common.collect.Maps;
import com.icfolson.aem.monitoring.core.model.RemoteSystem;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service(ClientDataRepository.class)
@Component(immediate = true)
public class ClientDataRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ClientDataRepository.class);

    private static final String CLIENT_PATH = "/etc/aem-monitoring/clients";

    @Reference
    private ResourceResolverFactory resolverFactory;

    public Map<String, RemoteSystem> getConfiguredSystems() {
        Map<String, RemoteSystem> out = new HashMap<>();
        try {
            final ResourceResolver resolver = resolverFactory.getServiceResourceResolver(Maps.newHashMap());
            final Resource parent = resolver.resolve(CLIENT_PATH);
            if (!ResourceUtil.isNonExistingResource(parent)) {
                for (final Resource child : parent.getChildren()) {
                    final RemoteSystem remoteSystem = child.adaptTo(RemoteSystem.class);
                    if (remoteSystem != null) {
                        out.put(child.getName(), remoteSystem);
                    }
                }
            }
        } catch (LoginException e) {
            LOG.error("Error logging in to repository", e);
        }
        return out;
    }

    public void setConfiguredSystem(final String name, final RemoteSystem configuration) {

        try {
            final ResourceResolver resolver = resolverFactory.getServiceResourceResolver(Maps.newHashMap());

            Map<String, Object> properties = new HashMap<>();
            properties.put("jcr:primaryType", "nt:unstructured");
            final Resource parent = ResourceUtil.getOrCreateResource(resolver, CLIENT_PATH, properties, "nt:unstructured", false);
            final String childName;
            if (name == null) {
                childName = ResourceUtil.createUniqueChildName(parent, "client");
            } else {
                childName = name;
            }
            Resource child = parent.getChild(childName);
            if (configuration == null && child != null) {
                resolver.delete(child);
            } else {
                properties = new HashMap<>();
                properties.put("host", configuration.getHost());
                properties.put("port", configuration.getPort());
                properties.put("user", configuration.getUser());
                properties.put("password", configuration.getPassword());
                if (child == null) {
                    properties.put("jcr:primaryType", "nt:unstructured");
                    resolver.create(parent, childName, properties);
                } else {
                    final ModifiableValueMap valueMap = child.adaptTo(ModifiableValueMap.class);
                    valueMap.putAll(properties);
                }
            }
            resolver.commit();
        } catch (LoginException e) {
            LOG.error("Error logging in to repository", e);
        } catch (PersistenceException e) {
            LOG.error("Error saving configuration data", e);
        }

    }

}
