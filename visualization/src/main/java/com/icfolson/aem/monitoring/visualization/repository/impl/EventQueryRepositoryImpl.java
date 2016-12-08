package com.icfolson.aem.monitoring.visualization.repository.impl;

import com.icfolson.aem.monitoring.core.constants.EventProperties;
import com.icfolson.aem.monitoring.core.time.TimeGrouper;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.Event;
import com.icfolson.aem.monitoring.database.generated.tables.EventProperty;
import com.icfolson.aem.monitoring.visualization.model.EventPropertyDescriptor;
import com.icfolson.aem.monitoring.visualization.model.EventQuery;
import com.icfolson.aem.monitoring.visualization.model.EventTypeDescriptor;
import com.icfolson.aem.monitoring.visualization.model.Predicate;
import com.icfolson.aem.monitoring.visualization.repository.EventQueryRepository;
import com.icfolson.aem.monitoring.visualization.result.EventListing;
import com.icfolson.aem.monitoring.visualization.result.FacetedTimeSeries;
import com.icfolson.aem.monitoring.visualization.result.TimeSeries;
import com.icfolson.aem.monitoring.visualization.util.JooqUtil;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectHavingStep;
import org.jooq.SelectSeekStep1;
import org.jooq.Table;
import org.jooq.conf.ParamType;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Component(immediate = true)
public class EventQueryRepositoryImpl implements EventQueryRepository {

    private static final Logger LOG = LoggerFactory.getLogger(EventQueryRepositoryImpl.class);

    private static final int LIMIT = 500;

    @Reference
    private ConnectionProvider connectionProvider;

    @Override
    public TimeSeries executeQuery(final EventQuery query) throws MonitoringDBException {

        final QueryEntities entities = prepareQueryEntities(query);

        try (final DSLContext context = getContext()) {

            // Prepare and execute the SQL query against the database
            final SelectHavingStep<Record> temp = context.select(entities.fields).from(entities.table)
                .where(entities.conditions).groupBy(entities.groupBy);
            final String sql = temp.getSQL(ParamType.INLINED);
            final Result<Record> records = context.fetch(sql);

            final TimeSeries timeSeries = new TimeSeries();
            entities.grouper.getPoints().forEach(value -> timeSeries.newPoint(value, 0).build()); // zero out points

            // Transfer data from returned records to TimeSeries DataPoints
            for (final Record record : records) {
                final Long binNumber = record.getValue(entities.fields.indexOf(entities.binField), Long.class);
                final Integer binCount = record.getValue(entities.fields.indexOf(entities.countField), Integer.class);
                long epoch = entities.grouper.getBinStartTime(binNumber);
                final TimeSeries.PointBuilder pointBuilder = timeSeries.newPoint(epoch, binCount);

                // When a y-axis property was requested, get the stats for the point
                if (entities.averageField != null) {
                    final Float average = record.get(entities.fields.indexOf(entities.averageField), Float.class);
                    if (average != null) {
                        pointBuilder.average(average);
                    }
                }
                pointBuilder.build();
            }
            return timeSeries;
        }
    }

    @Override
    public List<EventTypeDescriptor> getEventDescriptors() throws MonitoringDBException {
        final List<EventTypeDescriptor> out = new ArrayList<>();
        final Map<String, EventTypeDescriptor> map = new HashMap<>();
        try (final DSLContext context = getContext()) {
            final Result<Record4<String, String, Integer, Integer>> result = context
                .select(Tables.EVENT.TYPE,
                    Tables.EVENT_PROPERTY.NAME,
                    Tables.EVENT_PROPERTY.VALUE.count(),
                    Tables.EVENT_PROPERTY.REALVALUE.count())
                .from(Tables.EVENT)
                .join(Tables.EVENT_PROPERTY)
                .on(Tables.EVENT_PROPERTY.EVENT_ID.eq(Tables.EVENT.EVENT_ID))
                .groupBy(Tables.EVENT.TYPE, Tables.EVENT_PROPERTY.NAME)
                .fetch();
            for (final Record4<String, String, Integer, Integer> record : result) {
                final String eventName = record.value1();
                final String propertyName = record.value2();
                final boolean string = record.value3() > 0;
                final boolean real = record.value4() > 0;
                EventTypeDescriptor typeDescriptor = map.get(eventName);
                if (typeDescriptor == null) {
                    typeDescriptor = new EventTypeDescriptor(eventName);
                    map.put(eventName, typeDescriptor);
                    out.add(typeDescriptor);
                }
                final EventPropertyDescriptor propertyDescriptor =
                    new EventPropertyDescriptor(propertyName, string, real);
                typeDescriptor.getProperties().add(propertyDescriptor);
            }
        }
        return out;
    }

    @Override
    public FacetedTimeSeries executeFacetedQuery(final EventQuery query, final int maxFacetCount)
        throws MonitoringDBException {

        final FacetedTimeSeries out = new FacetedTimeSeries();
        final QueryEntities entities = prepareQueryEntities(query);
        try (final DSLContext context = getContext()) {
            final Condition maxFacets = entities.facetField.in(context
                .select(entities.facetField)
                .from(entities.table)
                .where(entities.conditions)
                .groupBy(entities.facetField)
                .orderBy(entities.facetField.count().desc())
                .limit(maxFacetCount));
            final List<Condition> conditionsWithMaxFacets = new ArrayList<>(entities.conditions);
            conditionsWithMaxFacets.add(maxFacets);
            final SelectSeekStep1<Record, Long> temp = context.select(entities.fields).from(entities.table)
                .where(conditionsWithMaxFacets).groupBy(entities.groupBy).orderBy(entities.binField);
            final String sql = temp.getSQL(ParamType.INLINED);
            final Result<Record> result = context.fetch(sql);
            for (final Record record : result) {
                final String facet = record.get(entities.fields.indexOf(entities.facetField), String.class);
                final Long binNumber = record.getValue(entities.fields.indexOf(entities.binField), Long.class);
                final Integer binCount = record.getValue(entities.fields.indexOf(entities.countField), Integer.class);
                long epoch = entities.grouper.getBinStartTime(binNumber);
                TimeSeries timeSeries = out.getTimeSeries(facet);
                if (timeSeries == null) {
                    TimeSeries ts = out.addFacet(facet); // Temporary final variable for lambda
                    entities.grouper.getPoints().forEach(value -> ts.newPoint(value, 0).build()); // zero out points
                    timeSeries = ts;
                }
                final TimeSeries.PointBuilder pointBuilder = timeSeries.newPoint(epoch, binCount);

                // When a y-axis property was requested, get the stats for the point
                if (entities.averageField != null) {
                    final Float average = record.get(entities.fields.indexOf(entities.averageField), Float.class);
                    if (average != null) {
                        pointBuilder.average(average);
                    }
                }
                pointBuilder.build();
            }
        }
        return out;
    }

    @Override
    public EventListing getEvents(final EventQuery query) throws MonitoringDBException {
        EventListing out = new EventListing();
        final QueryEntities entities = prepareQueryEntities(query);
        try (final DSLContext context = getContext()) {

            final SelectConditionStep<Record3<Long, Long, UUID>> events = context
                .select(Tables.EVENT.EVENT_ID, Tables.EVENT.TIME, Tables.EVENT.SYSTEM_ID)
                .from(entities.table)
                .where(entities.conditions);
            final String eventSQL = events.getSQL(ParamType.INLINED);
            final Result<Record> eventResults = context.fetch(eventSQL);
            for (final Record record : eventResults) {
                final long eventId = record.get(Tables.EVENT.EVENT_ID);
                final long timestamp = record.get(Tables.EVENT.TIME);
                final UUID systemID = record.get(Tables.EVENT.SYSTEM_ID);
                out.addEvent(eventId, timestamp, systemID);
            }

            final SelectConditionStep<Record4<Long, String, String, Float>> eventProperties = context
                .select(Tables.EVENT_PROPERTY.EVENT_ID, Tables.EVENT_PROPERTY.NAME, Tables.EVENT_PROPERTY.VALUE,
                    Tables.EVENT_PROPERTY.REALVALUE).from(Tables.EVENT_PROPERTY)
                .where(Tables.EVENT_PROPERTY.EVENT_ID.in(out.getEventIds()));
            final String eventPropertySQL = eventProperties.getSQL(ParamType.INLINED);
            final Result<Record> eventPropertyResults = context.fetch(eventPropertySQL);
            LOG.info("{} results returned", eventPropertyResults.size());
            for (final Record record : eventPropertyResults) {
                final Long eventId = record.get(Tables.EVENT_PROPERTY.EVENT_ID);
                final String name = record.get(Tables.EVENT_PROPERTY.NAME);
                final String value = record.get(Tables.EVENT_PROPERTY.VALUE);
                final Float realValue = record.get(Tables.EVENT_PROPERTY.REALVALUE);
                out.addEventProperty(eventId, name, value != null ? value : realValue);
            }
        }

        return out;
    }

    private DSLContext getContext() throws MonitoringDBException {
        return DSL.using(connectionProvider.getConnection(), SQLDialect.valueOf(connectionProvider.getSqlVariant()),
            new Settings().withRenderNameStyle(RenderNameStyle.AS_IS));
    }

    /**
     * Prepare the QueryEntities structure, used to execute the query and pull back results
     * @param query
     * @return
     */
    private static QueryEntities prepareQueryEntities(final EventQuery query) {
        final QueryEntities out = new QueryEntities();
        final long endEpoch = query.getWindowEnd() != null ? query.getWindowEnd() : System.currentTimeMillis();
        final long startEpoch = query.getWindowStart() != null ? query.getWindowStart()
            : endEpoch - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
        final int binCount = query.getBinCount();
        out.grouper = new TimeGrouper(startEpoch, endEpoch, binCount);

        out.conditions.add(Tables.EVENT.TIME.between(startEpoch, endEpoch));
        out.conditions.add(Tables.EVENT.TYPE.equal(query.getEventType()));
        out.binField = Tables.EVENT.TIME.sub(out.grouper.getStartEpoch()).divide(out.grouper.getBinLength());
        out.fields.add(out.binField);
        out.countField = out.binField.count();
        out.fields.add(out.countField);
        out.groupBy.add(out.binField);

        final Map<String, DynamicField> dynamicFields = new HashMap<>();
        for (final Predicate predicate : query.getPredicates()) {
            final String name = predicate.getPropertyName();
            DynamicField field = dynamicFields.get(name.toLowerCase());
            if (field == null) {
                field = new DynamicField(name);
                dynamicFields.put(field.name, field);
            }
            field.predicates.add(predicate);
        }
        if (query.getMetricProperty() != null) {
            final String yAxis = query.getMetricProperty();
            DynamicField field = dynamicFields.get(yAxis.toLowerCase());
            if (field == null) {
                field = new DynamicField(yAxis);
                dynamicFields.put(field.name, field);
            }
            field.metricField = true;
        }
        boolean systemFacet = false;
        if (query.getFacetProperty() != null) {
            final String facet = query.getFacetProperty();
            if (facet.startsWith(EventProperties.SYSTEM_PROPERTY_NS)) {
                systemFacet = true;
            } else {
                DynamicField field = dynamicFields.get(facet.toLowerCase());
                if (field == null) {
                    field = new DynamicField(facet);
                    dynamicFields.put(field.name, field);
                }
                field.facetField = true;
            }
        }

        out.table = Event.EVENT;
        for (final DynamicField field : dynamicFields.values()) {
            final EventProperty dynamicTable = Tables.EVENT_PROPERTY.as(field.getDynamicTableName());
            out.table = out.table.leftJoin(dynamicTable)
                .on(Tables.EVENT.EVENT_ID.equal(dynamicTable.EVENT_ID)
                    .and(dynamicTable.field(DSL.name("NAME"), String.class).equalIgnoreCase(field.name)));
            final Field<String> string = dynamicTable.field(DSL.name("VALUE"), String.class);
            final Field<Float> number = dynamicTable.field(DSL.name("REALVALUE"), Float.class);
            for (final Predicate predicate : field.predicates) {
                Field<?> f;
                switch (predicate.getOperation()) {
                case EQUAL:
                case NOT_EQUAL:
                case LIKE:
                    f = string;
                    break;
                default:
                    f = number;
                }
                final Condition condition = JooqUtil.getCondition(f, predicate.getOperation(), predicate.getValue());
                if (condition != null) {
                    out.conditions.add(condition);
                }
            }
            if (field.metricField) {
                out.averageField = number.avg();
                out.fields.add(out.averageField);
            }
            if (field.facetField) {
                out.facetField = string;
                out.fields.add(string);
                out.groupBy.add(string);
                out.facetCountField = string.count();
                out.fields.add(out.facetCountField);
            }
        }
        if (systemFacet) {
            final Field<String> string = Event.EVENT.SYSTEM_ID.cast(String.class);
            out.facetField = string;
            out.fields.add(string);
            out.groupBy.add(string);
            out.facetCountField = string.count();
            out.fields.add(out.facetCountField);
        }
        return out;
    }

    public static class QueryEntities {

        private Table<? extends Record> table;
        private TimeGrouper grouper;
        private Field<Long> binField;
        private Field<Integer> countField;
        private Field<BigDecimal> averageField;
        private Field<String> facetField;
        private Field<Integer> facetCountField;
        private final List<Field<?>> fields = new ArrayList<>();
        private final List<Condition> conditions = new ArrayList<>();
        private final List<Field<?>> groupBy = new ArrayList<>();

    }

    /**
     * A structure used to track an artificial field, dynamically pulled from the EVENT_PROPERTIES table and joined
     * to EVENT
     */
    public static class DynamicField {

        private static final String PREFIX = "DYN_";
        private static final String TABLE_POSTFIX = "_T";

        private final String name;
        private final List<Predicate> predicates = new ArrayList<>();
        private boolean metricField;
        private boolean facetField;

        private DynamicField(final String name) {
            this.name = name.toLowerCase();
        }

        private String getDynamicTableName() {
            return PREFIX + name.replace('.','_').toUpperCase() + TABLE_POSTFIX;
        }

    }

}
