package com.icfolson.aem.monitoring.reporting.system.impl;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.icfolson.aem.monitoring.core.constants.EventProperties;
import com.icfolson.aem.monitoring.database.connection.ConnectionProvider;
import com.icfolson.aem.monitoring.database.connection.ConnectionWrapper;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemPropertyRecord;
import com.icfolson.aem.monitoring.database.system.SystemInfo;
import com.icfolson.aem.monitoring.reporting.model.Operation;
import com.icfolson.aem.monitoring.reporting.model.Predicate;
import com.icfolson.aem.monitoring.reporting.system.SystemRepository;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.jooq.DSLContext;
import org.jooq.Result;

import java.util.*;

@Service
@Component(immediate = true)
public class SystemRepositoryImpl implements SystemRepository {

    // TODO add support for LIKE
    private static final Set<Operation> SUPPORTED = ImmutableSet.of(Operation.EQUAL, Operation.NOT_EQUAL);

    @Reference
    private ConnectionProvider connectionProvider;

    private Map<UUID, SystemInfoImpl> cache;

    @Override
    public Map<UUID, SystemInfo> getSystemInfo() throws MonitoringDBException {
        if (cache == null) {
            // TODO expire cache based on configured TTL
            cache = loadSystems();
        }
        return new HashMap<>(cache);
    }

    @Override
    public Map<String, Collection<String>> getDefinedSystemPropertyMappings() throws MonitoringDBException {
        Multimap<String, String> out = ArrayListMultimap.create();
        final Collection<SystemInfo> systems = getSystemInfo().values();
        systems.forEach(
            system -> system.getPropertyNames().forEach(
                name -> out.put(name, system.getPropertyValue(name))
            )
        );
        return out.asMap();
    }

    @Override
    public Set<UUID> getMatchingSystems(final List<Predicate> systemPredicates) throws MonitoringDBException {
        // For small system-sets, it is likely fine to maintain and query directly off in-memory cache, but larger
        // sets will require querying database directly
        for (final Predicate predicate : systemPredicates) {
            if (!SUPPORTED.contains(predicate.getOperation())) {
                throw new IllegalArgumentException("Operation not supported for system properties: "
                    + predicate.getOperation());
            }
            if (!predicate.getPropertyName().startsWith(EventProperties.SYSTEM_PROPERTY_NS)) {
                throw new IllegalArgumentException("Not a system property: " + predicate.getPropertyName());
            }
        }

        final Collection<SystemInfo> systems = getSystemInfo().values();
        final Set<UUID> matches = new HashSet<>();

        for (final SystemInfo system : systems) {
            boolean match = true;
            for (final Predicate predicate : systemPredicates) {

                final String systemValue = system.getPropertyValue(predicate.getPropertyName());
                final String queryValue = predicate.getValue();
                switch(predicate.getOperation()) {
                case EQUAL:
                    match = match && Objects.equal(systemValue, queryValue);
                    break;
                case NOT_EQUAL:
                    match = match && !Objects.equal(systemValue, queryValue);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal operation");
                }
                if (!match) {
                    break;
                }
            }
            if (match) {
                matches.add(system.getSystemId());
            }
        }

        return matches;
    }
    private ConnectionWrapper getConnection() throws MonitoringDBException {
        return connectionProvider.getConnection();
    }

    private Map<UUID, SystemInfoImpl> loadSystems() throws MonitoringDBException {
        final Map<UUID, SystemInfoImpl> out = new HashMap<>();
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final Result<SystemPropertyRecord> result = context.selectFrom(Tables.SYSTEM_PROPERTY).fetch();
            for (final SystemPropertyRecord propertyRecord : result) {
                final UUID uuid = propertyRecord.getSystemId();
                SystemInfoImpl systemInfo = out.get(uuid);
                if (systemInfo == null) {
                    systemInfo = new SystemInfoImpl(uuid);
                    out.put(uuid, systemInfo);
                }
                systemInfo.properties.put(EventProperties.SYSTEM_PROPERTY_NS + propertyRecord.getName(),
                    propertyRecord.getValue());
            }
        } catch (MonitoringDBException e) {
            throw new MonitoringDBException(e);
        }
        return out;
    }

    private class SystemInfoImpl implements SystemInfo {

        private final UUID systemId;
        private final Map<String, String> properties = new HashMap<>();

        private SystemInfoImpl(final UUID systemId) {
            this.systemId = systemId;
        }

        @Override
        public UUID getSystemId() {
            return systemId;
        }

        @Override
        public Set<String> getPropertyNames() {
            return Collections.unmodifiableSet(properties.keySet());
        }

        @Override
        public String getPropertyValue(final String propertyName) {
            return properties.get(propertyName);
        }
    }
    
    @Deactivate
    protected final void deactivate(final Map<String, Object> props) {
        cache = null;
    }
    
}
