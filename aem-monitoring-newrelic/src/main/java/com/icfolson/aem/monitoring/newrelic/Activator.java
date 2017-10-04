package com.icfolson.aem.monitoring.newrelic;

import com.newrelic.api.agent.NewRelic;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(final BundleContext context) throws Exception {
        try {
            NewRelic.getAgent();
        } catch (NoClassDefFoundError e) {
            LOG.info("New Relic not found -- New Relic bundle will be disabled");
            context.getBundle().stop();
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {

    }
}
