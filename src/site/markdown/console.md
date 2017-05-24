# AEM Monitoring Console

The AEM Monitoring console provides data visualization and analysis. It is available in all topologies utilizing local database storage (H2 or generic DB).  The console can be found at [`/apps/aem-monitoring/content/console.html`](http://localhost:4502/apps/aem-monitoring/content/console.html) on your author instance.  Note that access is restricted to the following AEM user groups: `administrators`, `aem-monitoring-user`, or `aem-monitoring-admin`.  The console functionality is split into three major views, outlined below.

## Events

The Events view visualizes recently-submitted events from various time periods. The events are plotted on a graph, allowing users to facet and filter the result set displayed.

Events are segregated by type, with only a single type being graphed at a time.  Users can switch between types using the select widget in the upper left of the console.  The next selector controls the time period displayed.  The final selector along the top of the console allows the user to switch between graphing the event count (the default), and other numeric fields (when applicable for the selected event type).

The left side of the console displays the facets.  Results can be faceted by any property submitted with a string value.  For example, sling request events can be faceted by the request extension.  Clicking on a facet will split the graph into facets according to values for that property included in the current dataset -- continuing with the request extension example, the facets might include "html", "js", "css", and "json".  Clicking on a facet will filter the dataset.  For example, clicking on the "html" facet would show only data for requests with an "html" extension.  Active filters appear as tags in the upper-right of the console, and can be removed by clicking on the "X" on each.

## Metrics

The Metrics view displays recently-submitted metrics data.  It displays a category listing to the right hand side.  Once selected, the average for that metric will be graphed for the selected time frame.  When operating in a topology with multiple instances, data for each instance will be displayed as a separate line.

## Counters

The Counters view is similar to the metrics view.  The only difference is that instead of displaying an average value, counters are shown as a rate.

