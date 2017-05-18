# Configurations

This document lists the configuration options exposed by the various AEM Monitoring bundles.

## Core Bundle

The core bundle is installed in all installation topologies.  It contains the following configurable services:

### Process Metrics Recorder

The Process Metrics Recorder samples the AEM Java Process and records various attributes as monitoring metrics.  The attributes recorded are:

* CPU Average Load
* Heap Memory Used
* Heap Memory Committed
* Non-Heap Memory Used
* Non-Heap Memory Committed

It exposes the following configuration options:

* Disable (boolean, default `false`): Whether to disable the recorder
* Sample Period (integer, default `15`): The number of seconds between each sample

### Replication Recorder

The Replication Recorder records replications on the instance as monitoring events with the following properties:

* user (string): The user ID that requested the replication
* type (string): The replication type ("ACTIVATE", "DEACTIVATE", or "DELETE")
* path (string): The path replicated

The service exposes the following configuration options:
* Disable (boolean, default `false`): Whether to disable the recorder

### Resource Change Recorder

The Resource Change Recorder records changes made to resources and monitoring events with the following properties:

* user (string); The user who updated the resource
* type (string): The change type ("ADDED", "REMOVED", or "CHANGED")
* path (string): The path of the changed resource

The service exposes the following configuration options:
* Disable (boolean, default `false`): Whether to disable the recorder
* Watched Paths (string, multiple, default \["/content", "/etc"\]): The paths that should be monitored for resource changes

### Sling Request Recorder

The Sling Request Recorder records incoming Sling requests as monitoring events with the following properties:

* request
  * user (string): The user ID that made the request
  * method (string): The method of the request
  * path (string): The path of the requested resource (if resolved)
  * selectors (string): The selector string (if any)
  * extension (string): The extension (if any)
  * suffix (string): The suffix (if any)
  * header (string): Captured headers (see configuration below)
* response
  * status (string): The response code returned
  * length (integer): The length, in bytes, of the response
* duration ms (integer): The duration, in milliseconds, of the transaction

The service exposes the following configuration options:
* Disable (boolean, default `false`): Whether to disable the recorder
* Captured Headers (string, multiple, default \["Host", "Referer"\]): The headers that should be captured with the monitoring event.  Captured headers will be recorded as additional event properties (with string values) named `request.header.HEADER_NAME`

### Logging Monitoring Writer

The Logging Monitoring Writer outputs all monitoring data to a log (on TRACE) for debugging.  The service exposes the following configuration options:
* Disable (boolean, default `false`): Whether to disable the logging writer

## Database Bundle

The database bundle is installed in H2 and Generic DB topologies (i.e., all except the New Relic topology).  It contains the following configurable services:

### Database Monitoring Writer

The Database Monitoring Writer outputs all monitoring data to the configured monitoring database via JDBC.  The service exposes the following configuration options:
* Disable (boolean, default `false`): Whether to disable the database writer

### Delete Old Data Task

The Delete Old Data Task is a scheduled task that deletes monitoring data older than a configured limit.  By default, it runs at midnight and deletes data older than 7 days.  The service exposes the following configuration options:
* Disable (boolean, default `false`): Whether to disable the task
* Storage Period (integer, default `7`): The length of time, in days, to retain monitoring data
* Task Schedule (string, default `0 0 0 * * ?`): A cron expression used to schedule the task

## H2 Bundle

The H2 bundle is only installed in the on-instance topologies.  It contains the following configurable services:

### H2 Server

The H2 Server service configures (and optionally manages) an H2 server instance for storage of monitoring data.  The service exposes the following configuration options:
* Server (string, default `localhost`): A server name or IP address used to connect to the server
* Server Port (int, default `8084` on author and `8085` on publish): The port that the server runs on
* H2 basedir (string, default `$SLING_HOME/db`): An optional base directory.
* Allow Remote (boolean, default `true`): Whether the server allows access to other instances.  Also required to access the DB using the H2 console.
* Externally Managed (boolean, default `false`): Check to prevent this service from starting/stopping the server.  The server must be started externally and accessible from the specified server/port.
 
 ### H2 Connection Provider
 
 The H2 Connection Provider service generates DB connections for the client code.  It uses the H2 Server configured above, and additionally specifies a database name, user, and password.  The service exposes the following configuration options:
 * DB Name (string, default `monitoring`): The database name to connect to
 * User (string, default 'sa'): The user to connect as
 * Password (string): The password to connect with
 
 ## Generic DB Bundle
 
The Generic DB bundle is only installed in the off-instance topology.  It contains the following configurable service:
 
 ### Generic Database Connection Provider
 
 The Generic Database Connection Provider assumes there is a configured Day Commons JDBC Connection Pool with a name `aem-monitoring`.  It's only purpose is to wrap the connections in a jOOQ wrapper that specifies the dialect of the connected database.  The service exposes the following configuration options:
* SQL Dialect (string, required): The SQL Dialect to use for the `aem-monitoring` database.  See [jOOQ Documentation](http://www.jooq.org/javadoc/3.6.2/org/jooq/SQLDialect.html) for available constants

## New Relic Bundle

The New Relic bundle is only installed in the New Relic topology.  It contains the following configurable service: 

### New Relic Monitoring Writer

The New Relic Monitoring Writer writes monitoring data to New Relic servers.  It assumes the New Relic agent is installed on the instance (and will fail if not).  The writer exposes the following configuration options:
* Disable (boolean, default `false`): Whether to disable the writer
