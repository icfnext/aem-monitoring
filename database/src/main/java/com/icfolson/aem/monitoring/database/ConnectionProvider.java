package com.icfolson.aem.monitoring.database;

import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.model.ConnectionWrapper;

public interface ConnectionProvider {

    ConnectionWrapper getConnection() throws MonitoringDBException;

}
