# Extension

AEM Monitoring provides 3 primary extension points for modifying monitoring behavior.

## Monitoring Writer
[MonitoringWriter](http://code.digitalatolson.com/aem-monitoring/apidocs/com/icfolson/aem/monitoring/core/writer/MonitoringWriter.html) is the service interface used to define OSGi components that write monitoring data to external systems.  Each writer must specify a unique name that can be used to apply filters to the writer (see below).  See [NewRelicWriter](https://github.com/OlsonDigital/aem-monitoring/blob/develop/newrelic/src/main/java/com/icfolson/aem/monitoring/newrelic/NewRelicWriter.java) for an example implementation.

## Monitoring Filter
[MonitoringFilter](http://code.digitalatolson.com/aem-monitoring/apidocs/com/icfolson/aem/monitoring/core/filter/MonitoringFilter.html) is an service interface that filters and transforms monitoring data that is input via [MonitoringService](http://code.digitalatolson.com/aem-monitoring/apidocs/com/icfolson/aem/monitoring/core/service/MonitoringService.html).  Monitoring Filters function similarly to [Sling Filters](https://sling.apache.org/documentation/the-sling-engine/filters.html) in that they provide a scope and an order.  The scope is the name of a writer (see above).  When a writer is not provided, the filter is applied as the data is input, before any writers are called.  See [NewRelicFilter](https://github.com/OlsonDigital/aem-monitoring/blob/develop/newrelic/src/main/java/com/icfolson/aem/monitoring/newrelic/NewRelicFilter.java) for an example implementation.

## System Info Provider
[SystemInfoProvider](http://code.digitalatolson.com/aem-monitoring/apidocs/com/icfolson/aem/monitoring/database/system/SystemInfoProvider.html) is an interface used to provide info about the system providing monitoring data.  In a clustered topology, where data is aggregated from several systems, this data is used to sort, facet, and filter data points.  See [AEMSystemInfoProvider](https://github.com/OlsonDigital/aem-monitoring/blob/develop/database/src/main/java/com/icfolson/aem/monitoring/database/system/impl/AEMSystemInfoProvider.java) for an example implementation.