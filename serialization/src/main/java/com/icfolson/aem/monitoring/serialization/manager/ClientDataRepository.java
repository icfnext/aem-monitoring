package com.icfolson.aem.monitoring.serialization.manager;

import com.icfolson.aem.monitoring.core.model.RemoteSystem;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;

import java.util.ArrayList;
import java.util.List;

@Service(ClientDataRepository.class)
@Component(immediate = true)
public class ClientDataRepository {

    private static final String CLIENT_PATH = "/etc/aem-monitoring/clients";

    @Reference
    private ResourceResolverFactory resolverFactory;

    public List<RemoteSystem> getConfiguredSystems() {
        List<RemoteSystem> out = new ArrayList<>();
        try {
            final ResourceResolver resolver = resolverFactory.getAdministrativeResourceResolver(null); //TODO
            final Resource parent = resolver.resolve(CLIENT_PATH);
            if (!ResourceUtil.isNonExistingResource(parent)) {
                for (final Resource child : parent.getChildren()) {
                    final RemoteSystem remoteSystem = child.adaptTo(RemoteSystem.class);
                    if (remoteSystem != null) {
                        out.add(remoteSystem);
                    }
                }
            }
        } catch (LoginException e) {
            e.printStackTrace();
        }
        return out;
    }

}
