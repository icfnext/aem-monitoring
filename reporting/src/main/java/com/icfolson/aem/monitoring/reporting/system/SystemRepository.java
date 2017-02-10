package com.icfolson.aem.monitoring.reporting.system;

import com.icfolson.aem.monitoring.database.system.SystemInfo;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.reporting.model.Predicate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface SystemRepository {

    Map<UUID, SystemInfo> getSystemInfo() throws MonitoringDBException;

    Map<String, Collection<String>> getDefinedSystemPropertyMappings() throws MonitoringDBException;

    Set<UUID> getMatchingSystems(List<Predicate> systemPredicates) throws MonitoringDBException;

}
