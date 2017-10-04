package com.icfolson.aem.monitoring.database.connection;

import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;

public interface ConnectionProvider {

    ConnectionWrapper getConnection() throws MonitoringDBException;

}
