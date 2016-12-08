/**
 * This class is generated by jOOQ
 */
package com.icfolson.aem.monitoring.database.generated;


import com.icfolson.aem.monitoring.database.generated.tables.CounterValue;
import com.icfolson.aem.monitoring.database.generated.tables.Event;
import com.icfolson.aem.monitoring.database.generated.tables.EventProperty;
import com.icfolson.aem.monitoring.database.generated.tables.MetricValue;
import com.icfolson.aem.monitoring.database.generated.tables.System;
import com.icfolson.aem.monitoring.database.generated.tables.SystemProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Sequence;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.8.6"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Monitoring extends SchemaImpl {

    private static final long serialVersionUID = 1112548199;

    /**
     * The reference instance of <code>MONITORING</code>
     */
    public static final Monitoring MONITORING = new Monitoring();

    /**
     * The table <code>MONITORING.SYSTEM</code>.
     */
    public final System SYSTEM = com.icfolson.aem.monitoring.database.generated.tables.System.SYSTEM;

    /**
     * The table <code>MONITORING.EVENT</code>.
     */
    public final Event EVENT = com.icfolson.aem.monitoring.database.generated.tables.Event.EVENT;

    /**
     * The table <code>MONITORING.EVENT_PROPERTY</code>.
     */
    public final EventProperty EVENT_PROPERTY = com.icfolson.aem.monitoring.database.generated.tables.EventProperty.EVENT_PROPERTY;

    /**
     * The table <code>MONITORING.COUNTER_VALUE</code>.
     */
    public final CounterValue COUNTER_VALUE = com.icfolson.aem.monitoring.database.generated.tables.CounterValue.COUNTER_VALUE;

    /**
     * The table <code>MONITORING.METRIC_VALUE</code>.
     */
    public final MetricValue METRIC_VALUE = com.icfolson.aem.monitoring.database.generated.tables.MetricValue.METRIC_VALUE;

    /**
     * The table <code>MONITORING.SYSTEM_PROPERTY</code>.
     */
    public final SystemProperty SYSTEM_PROPERTY = com.icfolson.aem.monitoring.database.generated.tables.SystemProperty.SYSTEM_PROPERTY;

    /**
     * No further instances allowed
     */
    private Monitoring() {
        super("MONITORING", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        List result = new ArrayList();
        result.addAll(getSequences0());
        return result;
    }

    private final List<Sequence<?>> getSequences0() {
        return Arrays.<Sequence<?>>asList(
            Sequences.SYSTEM_SEQUENCE_98728855_9885_43B2_BF93_50A3899BC107,
            Sequences.SYSTEM_SEQUENCE_A1B396BD_02EF_4B35_AE49_A8A31EFAB2FC,
            Sequences.SYSTEM_SEQUENCE_D5CA9CF8_9D3C_495E_B347_7BB9C42703A6);
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            System.SYSTEM,
            Event.EVENT,
            EventProperty.EVENT_PROPERTY,
            CounterValue.COUNTER_VALUE,
            MetricValue.METRIC_VALUE,
            SystemProperty.SYSTEM_PROPERTY);
    }
}
