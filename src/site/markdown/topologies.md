# Supported Topologies

AEM Monitoring supports 4 primary topologies withing an AEM environment: Multiple on-instance, single on-instance, single off-instance, and New Relic.  The on-instance topologies use an [H2](http://www.h2database.com) database stored on the AEM server itself (and utilizing the machine's own storage), whereas off-instance configurations can utilize several other database engines.  MySQL, MariaDB, and Aurora have been tested and verified as off-instance database engines, but in theory any database supported by [jOOQ](https://www.jooq.org/) can be used.  The New Relic configuration requires a [New Relic](https://newrelic.com/) subscription, and installation of the [Java agent](https://docs.newrelic.com/docs/agents/java-agent) on all instances. Setup and usage for each of the configurations is discussed below.

It is worth noting that different environments can support different topologies without differing your code base.  For example, a common setup might involve developers working with an H2 setup on their local instances, while higher environments are configured to use a New Relic topology.

## Multiple On-Instance
Multiple on-instance topologies use a separate H2 database for each AEM instance.  A service on author calls servlets on the publish instances to "reverse replicate" monitoring data to the author instance, where the AEM Monitoring console is accessed.  The benefit of this topology is that data from the publish instance(s) will not be lost when the author instance is offline.  The downside is that there is a small overhead on author and publish instances for the data replication.

This configuration can be set up by installing the `aem-monitoring-ui` and `aem-monitoring-h2` packages on each AEM instance.  The H2 package contains configurations to start an H2 server and auto-initialize the schema, using port 8084 for author and 8085 for publish (to avoid conflicts in local setups).  Additionally, serialization will need to be configured from publish to author.  Go to [the console](http://localhost:4502/apps/aem-monitoring/content/console.html) on author and select the "Config" tab, creating client configurations for each publish instance.

![Client Configuration Screen](https://github.com/OlsonDigital/aem-monitoring/raw/develop/src/site/images/config.png "Client Configuration Screen")

## Single On-Instance
A single on-instance topology will have data from all instance written directly to an H2 server running on author.  This configuration eliminates the overhead of serializing data between instances, as the publish instances write directly to the single database via the H2 server managed by the author instance.  The downside of this configuration is that data from the publish instances will be lost while the author instance is down.

To configure this topology, first remove all clients from the configuration screen (above), as publish instances will be writing directly to the database.  Next, configure the "AEM Monitoring: H2 DB Server" service in the Sling console. Select "Externally Managed" and enter the author server address and port (8084 by default) to route publish data directly to the database managed by the author instance.

![H2 Server Configuration](https://github.com/OlsonDigital/aem-monitoring/raw/develop/src/site/images/h2-server.png "H2 Server Configuration")

## Off-Instance
The off-instance topology writes all data to a single externally-managed database instance.  This is the most stable and available configuration, but requires additional setup.

For MySQL-based DBs, [this SQL script](https://github.com/OlsonDigital/aem-monitoring/blob/develop/generic-db/src/main/resources/mysql-init.sql) can be used to initialize the monitoring database schema.  Install both the `aem-monitoring-ui` and `aem-monitoring-generic-db-installation` packages.  Create a "Day Commons JDBC Connection Pool" configuration for your database, using `aem-monitoring` as the datasource name, and ensuring that the JDBC driver for your database is installed in the OSGi environment.  Finally, configure the "AEM Monitoring: Generic DB Connection Provider" service, entering the appropriate Dialect constant for jOOQ from [this page](https://www.jooq.org/javadoc/3.6.2/org/jooq/SQLDialect.html).

![JDBC Configuration](https://github.com/OlsonDigital/aem-monitoring/raw/develop/src/site/images/jdbc.png "JDBC Configuration")

## New Relic
The New Relic topology delegates all data storage and visualization/reporting to New Relic's [Insights](https://newrelic.com/insights) services.
