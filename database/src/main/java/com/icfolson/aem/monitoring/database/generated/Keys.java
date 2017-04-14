/**
 * This class is generated by jOOQ
 */
package com.icfolson.aem.monitoring.database.generated;


import com.icfolson.aem.monitoring.database.generated.tables.Counter;
import com.icfolson.aem.monitoring.database.generated.tables.CounterValue;
import com.icfolson.aem.monitoring.database.generated.tables.Event;
import com.icfolson.aem.monitoring.database.generated.tables.EventProperty;
import com.icfolson.aem.monitoring.database.generated.tables.EventType;
import com.icfolson.aem.monitoring.database.generated.tables.Metric;
import com.icfolson.aem.monitoring.database.generated.tables.MetricValue;
import com.icfolson.aem.monitoring.database.generated.tables.System;
import com.icfolson.aem.monitoring.database.generated.tables.SystemProperty;
import com.icfolson.aem.monitoring.database.generated.tables.records.CounterRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.CounterValueRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventTypeRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.MetricRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.MetricValueRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemPropertyRecord;
import com.icfolson.aem.monitoring.database.generated.tables.records.SystemRecord;

import javax.annotation.Generated;

import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;


/**
 * A class modelling foreign key relationships between tables of the <code>MONITORING</code> 
 * schema
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.8.6"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<EventTypeRecord, Short> IDENTITY_EVENT_TYPE = Identities0.IDENTITY_EVENT_TYPE;
    public static final Identity<EventRecord, Long> IDENTITY_EVENT = Identities0.IDENTITY_EVENT;
    public static final Identity<CounterRecord, Short> IDENTITY_COUNTER = Identities0.IDENTITY_COUNTER;
    public static final Identity<MetricRecord, Short> IDENTITY_METRIC = Identities0.IDENTITY_METRIC;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<SystemRecord> CONSTRAINT_9 = UniqueKeys0.CONSTRAINT_9;
    public static final UniqueKey<EventTypeRecord> CONSTRAINT_5 = UniqueKeys0.CONSTRAINT_5;
    public static final UniqueKey<EventTypeRecord> UNIQUE_EVENT_TYPE_NAME = UniqueKeys0.UNIQUE_EVENT_TYPE_NAME;
    public static final UniqueKey<EventRecord> CONSTRAINT_3 = UniqueKeys0.CONSTRAINT_3;
    public static final UniqueKey<CounterRecord> CONSTRAINT_6 = UniqueKeys0.CONSTRAINT_6;
    public static final UniqueKey<CounterRecord> UNIQUE_COUNTER_NAME = UniqueKeys0.UNIQUE_COUNTER_NAME;
    public static final UniqueKey<MetricRecord> CONSTRAINT_8 = UniqueKeys0.CONSTRAINT_8;
    public static final UniqueKey<MetricRecord> UNIQUE_METRIC_NAME = UniqueKeys0.UNIQUE_METRIC_NAME;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<SystemPropertyRecord, SystemRecord> CONSTRAINT_1 = ForeignKeys0.CONSTRAINT_1;
    public static final ForeignKey<EventRecord, SystemRecord> CONSTRAINT_3F = ForeignKeys0.CONSTRAINT_3F;
    public static final ForeignKey<EventPropertyRecord, EventRecord> CONSTRAINT_92 = ForeignKeys0.CONSTRAINT_92;
    public static final ForeignKey<CounterValueRecord, CounterRecord> CONSTRAINT_E9 = ForeignKeys0.CONSTRAINT_E9;
    public static final ForeignKey<CounterValueRecord, SystemRecord> CONSTRAINT_E = ForeignKeys0.CONSTRAINT_E;
    public static final ForeignKey<MetricValueRecord, MetricRecord> CONSTRAINT_368 = ForeignKeys0.CONSTRAINT_368;
    public static final ForeignKey<MetricValueRecord, SystemRecord> CONSTRAINT_36 = ForeignKeys0.CONSTRAINT_36;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 extends AbstractKeys {
        public static Identity<EventTypeRecord, Short> IDENTITY_EVENT_TYPE = createIdentity(EventType.EVENT_TYPE, EventType.EVENT_TYPE.EVENT_TYPE_ID);
        public static Identity<EventRecord, Long> IDENTITY_EVENT = createIdentity(Event.EVENT, Event.EVENT.EVENT_ID);
        public static Identity<CounterRecord, Short> IDENTITY_COUNTER = createIdentity(Counter.COUNTER, Counter.COUNTER.COUNTER_ID);
        public static Identity<MetricRecord, Short> IDENTITY_METRIC = createIdentity(Metric.METRIC, Metric.METRIC.METRIC_ID);
    }

    private static class UniqueKeys0 extends AbstractKeys {
        public static final UniqueKey<SystemRecord> CONSTRAINT_9 = createUniqueKey(System.SYSTEM, "CONSTRAINT_9", System.SYSTEM.SYSTEM_ID);
        public static final UniqueKey<EventTypeRecord> CONSTRAINT_5 = createUniqueKey(EventType.EVENT_TYPE, "CONSTRAINT_5", EventType.EVENT_TYPE.EVENT_TYPE_ID);
        public static final UniqueKey<EventTypeRecord> UNIQUE_EVENT_TYPE_NAME = createUniqueKey(EventType.EVENT_TYPE, "UNIQUE_EVENT_TYPE_NAME", EventType.EVENT_TYPE.EVENT_NAME);
        public static final UniqueKey<EventRecord> CONSTRAINT_3 = createUniqueKey(Event.EVENT, "CONSTRAINT_3", Event.EVENT.EVENT_ID);
        public static final UniqueKey<CounterRecord> CONSTRAINT_6 = createUniqueKey(Counter.COUNTER, "CONSTRAINT_6", Counter.COUNTER.COUNTER_ID);
        public static final UniqueKey<CounterRecord> UNIQUE_COUNTER_NAME = createUniqueKey(Counter.COUNTER, "UNIQUE_COUNTER_NAME", Counter.COUNTER.COUNTER_NAME);
        public static final UniqueKey<MetricRecord> CONSTRAINT_8 = createUniqueKey(Metric.METRIC, "CONSTRAINT_8", Metric.METRIC.METRIC_ID);
        public static final UniqueKey<MetricRecord> UNIQUE_METRIC_NAME = createUniqueKey(Metric.METRIC, "UNIQUE_METRIC_NAME", Metric.METRIC.METRIC_NAME);
    }

    private static class ForeignKeys0 extends AbstractKeys {
        public static final ForeignKey<SystemPropertyRecord, SystemRecord> CONSTRAINT_1 = createForeignKey(com.icfolson.aem.monitoring.database.generated.Keys.CONSTRAINT_9, SystemProperty.SYSTEM_PROPERTY, "CONSTRAINT_1", SystemProperty.SYSTEM_PROPERTY.SYSTEM_ID);
        public static final ForeignKey<EventRecord, SystemRecord> CONSTRAINT_3F = createForeignKey(com.icfolson.aem.monitoring.database.generated.Keys.CONSTRAINT_9, Event.EVENT, "CONSTRAINT_3F", Event.EVENT.SYSTEM_ID);
        public static final ForeignKey<EventPropertyRecord, EventRecord> CONSTRAINT_92 = createForeignKey(com.icfolson.aem.monitoring.database.generated.Keys.CONSTRAINT_3, EventProperty.EVENT_PROPERTY, "CONSTRAINT_92", EventProperty.EVENT_PROPERTY.EVENT_ID);
        public static final ForeignKey<CounterValueRecord, CounterRecord> CONSTRAINT_E9 = createForeignKey(com.icfolson.aem.monitoring.database.generated.Keys.CONSTRAINT_6, CounterValue.COUNTER_VALUE, "CONSTRAINT_E9", CounterValue.COUNTER_VALUE.COUNTER_ID);
        public static final ForeignKey<CounterValueRecord, SystemRecord> CONSTRAINT_E = createForeignKey(com.icfolson.aem.monitoring.database.generated.Keys.CONSTRAINT_9, CounterValue.COUNTER_VALUE, "CONSTRAINT_E", CounterValue.COUNTER_VALUE.SYSTEM_ID);
        public static final ForeignKey<MetricValueRecord, MetricRecord> CONSTRAINT_368 = createForeignKey(com.icfolson.aem.monitoring.database.generated.Keys.CONSTRAINT_8, MetricValue.METRIC_VALUE, "CONSTRAINT_368", MetricValue.METRIC_VALUE.METRIC_ID);
        public static final ForeignKey<MetricValueRecord, SystemRecord> CONSTRAINT_36 = createForeignKey(com.icfolson.aem.monitoring.database.generated.Keys.CONSTRAINT_9, MetricValue.METRIC_VALUE, "CONSTRAINT_36", MetricValue.METRIC_VALUE.SYSTEM_ID);
    }
}
