/**
 * This class is generated by jOOQ
 */
package com.icfolson.aem.monitoring.database.generated.tables.records;


import com.icfolson.aem.monitoring.database.generated.tables.CounterValue;

import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


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
public class CounterValueRecord extends UpdatableRecordImpl<CounterValueRecord> implements Record5<Long, UUID, String, Long, Integer> {

    private static final long serialVersionUID = 1328214730;

    /**
     * Setter for <code>MONITORING.COUNTER_VALUE.COUNTER_VALUE_ID</code>.
     */
    public void setCounterValueId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>MONITORING.COUNTER_VALUE.COUNTER_VALUE_ID</code>.
     */
    public Long getCounterValueId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>MONITORING.COUNTER_VALUE.SYSTEM_ID</code>.
     */
    public void setSystemId(UUID value) {
        set(1, value);
    }

    /**
     * Getter for <code>MONITORING.COUNTER_VALUE.SYSTEM_ID</code>.
     */
    public UUID getSystemId() {
        return (UUID) get(1);
    }

    /**
     * Setter for <code>MONITORING.COUNTER_VALUE.METRIC_NAME</code>.
     */
    public void setMetricName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>MONITORING.COUNTER_VALUE.METRIC_NAME</code>.
     */
    public String getMetricName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>MONITORING.COUNTER_VALUE.TIME</code>.
     */
    public void setTime(Long value) {
        set(3, value);
    }

    /**
     * Getter for <code>MONITORING.COUNTER_VALUE.TIME</code>.
     */
    public Long getTime() {
        return (Long) get(3);
    }

    /**
     * Setter for <code>MONITORING.COUNTER_VALUE.INCREMENT_VALUE</code>.
     */
    public void setIncrementValue(Integer value) {
        set(4, value);
    }

    /**
     * Getter for <code>MONITORING.COUNTER_VALUE.INCREMENT_VALUE</code>.
     */
    public Integer getIncrementValue() {
        return (Integer) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Long, UUID, String, Long, Integer> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Long, UUID, String, Long, Integer> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return CounterValue.COUNTER_VALUE.COUNTER_VALUE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field2() {
        return CounterValue.COUNTER_VALUE.SYSTEM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return CounterValue.COUNTER_VALUE.METRIC_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field4() {
        return CounterValue.COUNTER_VALUE.TIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field5() {
        return CounterValue.COUNTER_VALUE.INCREMENT_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getCounterValueId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID value2() {
        return getSystemId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getMetricName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value4() {
        return getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value5() {
        return getIncrementValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterValueRecord value1(Long value) {
        setCounterValueId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterValueRecord value2(UUID value) {
        setSystemId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterValueRecord value3(String value) {
        setMetricName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterValueRecord value4(Long value) {
        setTime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterValueRecord value5(Integer value) {
        setIncrementValue(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterValueRecord values(Long value1, UUID value2, String value3, Long value4, Integer value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached CounterValueRecord
     */
    public CounterValueRecord() {
        super(CounterValue.COUNTER_VALUE);
    }

    /**
     * Create a detached, initialised CounterValueRecord
     */
    public CounterValueRecord(Long counterValueId, UUID systemId, String metricName, Long time, Integer incrementValue) {
        super(CounterValue.COUNTER_VALUE);

        set(0, counterValueId);
        set(1, systemId);
        set(2, metricName);
        set(3, time);
        set(4, incrementValue);
    }
}
