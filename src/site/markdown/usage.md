# Usage

Although AEM Monitoring provides several types monitoring of out of the box, adding custom data from your domain is simple.  The primary service interface for recording metrics is [MonitoringService](http://code.digitalatolson.com/aem-monitoring/apidocs/com/icfolson/aem/monitoring/core/service/MonitoringService.html). Callers can leverage this service to record events, metrics, and counters:

```java
import com.icfolson.aem.monitoring.core.model.base.DefaultMonitoringEvent;
import com.icfolson.aem.monitoring.core.model.MonitoringEvent;
import com.icfolson.aem.monitoring.core.model.QualifiedName;
import com.icfolson.aem.monitoring.core.service.MonitoringService;

public abstract class MonitoredComponent {
    
    private static final QualifiedName SOME_VALUE = new QualifiedName("some", "value");
    private static final QualifiedName EVENT_NAME = new QualifiedName("event");
    private static final QualifiedName THING_DONE = new QualifiedName("thing", "done");
    
    @Reference
    private MonitoringService monitoringService;
    
    public void doThing(final float someValue, final String anotherValue) {
        monitoringService.recordMetric(SOME_VALUE, someValue);
        
        final MonitoringEvent event = new DefaultMonitoringEvent(EVENT_NAME);
        event.setProperty("foo", someValue);
        event.setProperty("bar", anotherValue);
        
        final long start = System.currentTimeMillis();
        final String returnValue = actuallyDoThing();
        final long end = System.currentTimeMillis();
        
        event.setProperty("duration", end - start);
        event.setProperty("return", returnValue);
        monitoringService.recordEvent(event);
        
        monitoringService.incrementCounter(THING_DONE);
    }
    
    protected abstract String actuallyDoThing();
    
}

```
