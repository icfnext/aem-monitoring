/**
 * This class is generated by jOOQ
 */
package com.icfolson.aem.monitoring.database.generated.tables.records;


import com.icfolson.aem.monitoring.database.generated.tables.Metric;

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
public class MetricRecord extends UpdatableRecordImpl<MetricRecord> implements Record3<Long, Long, String> {

    private static final long serialVersionUID = -177910005;

    /**
     * Setter for <code>MONITORING.METRIC.METRIC_ID</code>.
     */
    public void setMetricId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>MONITORING.METRIC.METRIC_ID</code>.
     */
    public Long getMetricId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>MONITORING.METRIC.PARENT_METRIC_ID</code>.
     */
    public void setParentMetricId(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>MONITORING.METRIC.PARENT_METRIC_ID</code>.
     */
    public Long getParentMetricId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>MONITORING.METRIC.METRIC_NAME</code>.
     */
    public void setMetricName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>MONITORING.METRIC.METRIC_NAME</code>.
     */
    public String getMetricName() {
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
        return Metric.METRIC.METRIC_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return Metric.METRIC.PARENT_METRIC_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Metric.METRIC.METRIC_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getMetricId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value2() {
        return getParentMetricId();
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
    public MetricRecord value1(Long value) {
        setMetricId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetricRecord value2(Long value) {
        setParentMetricId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetricRecord value3(String value) {
        setMetricName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetricRecord values(Long value1, Long value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MetricRecord
     */
    public MetricRecord() {
        super(Metric.METRIC);
    }

    /**
     * Create a detached, initialised MetricRecord
     */
    public MetricRecord(Long metricId, Long parentMetricId, String metricName) {
        super(Metric.METRIC);

        set(0, metricId);
        set(1, parentMetricId);
        set(2, metricName);
    }
}
