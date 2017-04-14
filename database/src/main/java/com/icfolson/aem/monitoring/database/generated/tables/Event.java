/**
 * This class is generated by jOOQ
 */
package com.icfolson.aem.monitoring.database.generated.tables;


import com.icfolson.aem.monitoring.database.generated.Keys;
import com.icfolson.aem.monitoring.database.generated.Monitoring;
import com.icfolson.aem.monitoring.database.generated.tables.records.EventRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


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
public class Event extends TableImpl<EventRecord> {

    private static final long serialVersionUID = 121746387;

    /**
     * The reference instance of <code>MONITORING.EVENT</code>
     */
    public static final Event EVENT = new Event();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EventRecord> getRecordType() {
        return EventRecord.class;
    }

    /**
     * The column <code>MONITORING.EVENT.EVENT_ID</code>.
     */
    public final TableField<EventRecord, Long> EVENT_ID = createField("EVENT_ID", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("(NEXT VALUE FOR MONITORING.SYSTEM_SEQUENCE_7BBDC9B6_92AF_4EF5_80C2_AFC2A521B4A4)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>MONITORING.EVENT.EVENT_TYPE_ID</code>.
     */
    public final TableField<EventRecord, Short> EVENT_TYPE_ID = createField("EVENT_TYPE_ID", org.jooq.impl.SQLDataType.SMALLINT.nullable(false), this, "");

    /**
     * The column <code>MONITORING.EVENT.SYSTEM_ID</code>.
     */
    public final TableField<EventRecord, String> SYSTEM_ID = createField("SYSTEM_ID", org.jooq.impl.SQLDataType.CHAR.length(36).nullable(false), this, "");

    /**
     * The column <code>MONITORING.EVENT.TIME</code>.
     */
    public final TableField<EventRecord, Long> TIME = createField("TIME", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>MONITORING.EVENT</code> table reference
     */
    public Event() {
        this("EVENT", null);
    }

    /**
     * Create an aliased <code>MONITORING.EVENT</code> table reference
     */
    public Event(String alias) {
        this(alias, EVENT);
    }

    private Event(String alias, Table<EventRecord> aliased) {
        this(alias, aliased, null);
    }

    private Event(String alias, Table<EventRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Monitoring.MONITORING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<EventRecord, Long> getIdentity() {
        return Keys.IDENTITY_EVENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<EventRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<EventRecord>> getKeys() {
        return Arrays.<UniqueKey<EventRecord>>asList(Keys.CONSTRAINT_3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<EventRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<EventRecord, ?>>asList(Keys.CONSTRAINT_3F);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Event as(String alias) {
        return new Event(alias, this);
    }

    /**
     * Rename this table
     */
    public Event rename(String name) {
        return new Event(name, null);
    }
}
