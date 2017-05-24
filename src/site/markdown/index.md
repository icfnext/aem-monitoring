# AEM Monitoring
AEM Monitoring is an integrated software analytics solution for AEM.  It provides a simple interface for recording custom events, metrics, and counters, and a UI for analyzing data produced by your application.  The solution can be configured to use one of several potential storage engines for monitoring data, including several database engines, as well as New Relic.  When using database storage, a console is provided for viewing/analysis of monitoring data from the author instance.

![Event Visualization](https://github.com/OlsonDigital/aem-monitoring/raw/develop/src/site/images/events.png "Event Visualization")

## Use Cases
- Monitor external integrations (response time, error codes, etc.) to aid in troubleshooting issues
- Who deleted a component or deactivated a page? Track various AEM actions by user ID
- Track AEM page renders to find caching inefficiencies
- View system performance metrics, usage statistics, etc
- Easy integration with New Relic monitoring, using a single abstraction for both New Relic and local-only reporting
- Use in place of INFO logging to track any complex domain event

## Features
- High performance: capable of processing 100K complex events per hour
- Simple extension: Create your own events and properties properties, metrics, and counters with no pre-configuration
- Store data for as long as needed, with configurable automatic deletion

## Installation / Setup

See [Topologies](topologies.html) for a listing of potential system configurations and installation instructions for each.  See [Configurations](configuration.html) for a listing of the configuration options exposed.