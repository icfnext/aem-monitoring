package com.icfolson.aem.monitoring.database;

import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;

import java.sql.Connection;

public interface ConnectionProvider {

    String getSqlVariant();

    Connection getConnection() throws MonitoringDBException;

}
