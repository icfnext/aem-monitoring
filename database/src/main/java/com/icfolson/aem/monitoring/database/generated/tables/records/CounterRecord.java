/**
 * This class is generated by jOOQ
 */
package com.icfolson.aem.monitoring.database.generated.tables.records;


import com.icfolson.aem.monitoring.database.generated.tables.Counter;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
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
public class CounterRecord extends UpdatableRecordImpl<CounterRecord> implements Record3<Long, Long, String> {

    private static final long serialVersionUID = -1803493195;

    /**
     * Setter for <code>MONITORING.COUNTER.COUNTER_ID</code>.
     */
    public void setCounterId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>MONITORING.COUNTER.COUNTER_ID</code>.
     */
    public Long getCounterId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>MONITORING.COUNTER.PARENT_COUNTER_ID</code>.
     */
    public void setParentCounterId(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>MONITORING.COUNTER.PARENT_COUNTER_ID</code>.
     */
    public Long getParentCounterId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>MONITORING.COUNTER.COUNTER_NAME</code>.
     */
    public void setCounterName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>MONITORING.COUNTER.COUNTER_NAME</code>.
     */
    public String getCounterName() {
        return (String) get(2);
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
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Long, Long, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Long, Long, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return Counter.COUNTER.COUNTER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return Counter.COUNTER.PARENT_COUNTER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Counter.COUNTER.COUNTER_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getCounterId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value2() {
        return getParentCounterId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getCounterName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterRecord value1(Long value) {
        setCounterId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterRecord value2(Long value) {
        setParentCounterId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterRecord value3(String value) {
        setCounterName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CounterRecord values(Long value1, Long value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached CounterRecord
     */
    public CounterRecord() {
        super(Counter.COUNTER);
    }

    /**
     * Create a detached, initialised CounterRecord
     */
    public CounterRecord(Long counterId, Long parentCounterId, String counterName) {
        super(Counter.COUNTER);

        set(0, counterId);
        set(1, parentCounterId);
        set(2, counterName);
    }
}
