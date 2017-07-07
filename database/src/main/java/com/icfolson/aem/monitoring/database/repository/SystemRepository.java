package com.icfolson.aem.monitoring.database.repository;

import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;

public interface SystemRepository {

    short getSystemId(String repositoryUuid) throws MonitoringDBException;

    String getRepositoryUuid(final short systemId) throws MonitoringDBException;
}
