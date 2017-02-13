package com.icfolson.aem.monitoring.test;

interface LoadTestService {

    void execute();

    void setEventsPerSecond(int eventsPerSecond);

    void setTestDuration(int testDuration);

    void setStringProperties(int stringProperties);

    void setStringPropertyValues(int stringPropertyValues);

    void setRealProperties(int realProperties);
    
}
