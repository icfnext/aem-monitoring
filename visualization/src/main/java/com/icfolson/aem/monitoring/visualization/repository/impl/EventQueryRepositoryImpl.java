package com.icfolson.aem.monitoring.visualization.repository.impl;

import com.icfolson.aem.monitoring.core.constants.EventProperties;
import com.icfolson.aem.monitoring.core.time.TimeGrouper;
import com.icfolson.aem.monitoring.database.ConnectionProvider;
import com.icfolson.aem.monitoring.database.exception.MonitoringDBException;
import com.icfolson.aem.monitoring.database.generated.Tables;
import com.icfolson.aem.monitoring.database.generated.tables.Event;
import com.icfolson.aem.monitoring.database.generated.tables.EventProperty;
import com.icfolson.aem.monitoring.database.model.ConnectionWrapper;
import com.icfolson.aem.monitoring.database.repository.EventRepository;
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
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Component(immediate = true)
public class EventQueryRepositoryImpl implements EventQueryRepository {

    private static final Logger LOG = LoggerFactory.getLogger(EventQueryRepositoryImpl.class);

    @Reference
    private ConnectionProvider connectionProvider;

    @Reference
    private EventRepository repository;

    @Override
    public TimeSeries executeQuery(final EventQuery query) throws MonitoringDBException {

        final QueryEntities entities = prepareQueryEntities(query);

        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();

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
        final Map<String, EventTypeDescriptor> typeMap = new HashMap<>();
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            final Map<String, Map<String, EventPropertyDescriptor>> propertyMap = new HashMap<>();

            TableOnConditionStep<Record> table = Tables.EVENT_TYPE.join(Tables.EVENT)
                .on(Tables.EVENT.EVENT_TYPE_ID.eq(Tables.EVENT_TYPE.EVENT_TYPE_ID));
            table = table.join(Tables.EVENT_PROPERTY)
                .on(Tables.EVENT.EVENT_ID.eq(Tables.EVENT_PROPERTY.EVENT_ID));
            final Result<Record5<String, Short, String, Integer, Integer>> result = context
                .select(Tables.EVENT_TYPE.EVENT_NAME,
                    Tables.EVENT_TYPE.EVENT_TYPE_ID,
                    Tables.EVENT_PROPERTY.NAME,
                    Tables.EVENT_PROPERTY.VALUE.count(),
                    Tables.EVENT_PROPERTY.REALVALUE.count()).from(table)
                .groupBy(Tables.EVENT_TYPE.EVENT_NAME, Tables.EVENT_PROPERTY.NAME).fetch();

            for (final Record5<String, Short, String, Integer, Integer> record : result) {
                final String typeName = record.value1();
                final Short typeId = record.value2();
                final String propertyName = record.value3();
                final boolean string = record.value4() > 0;
                final boolean real = record.value5() > 0;
                EventTypeDescriptor typeDescriptor = typeMap.get(typeName);
                Map<String, EventPropertyDescriptor> propertiesByName = propertyMap.get(typeName);
                if (typeDescriptor == null) {
                    typeDescriptor = new EventTypeDescriptor(typeId, typeName);
                    typeMap.put(typeName, typeDescriptor);
                    out.add(typeDescriptor);
                    propertiesByName = new HashMap<>();
                    propertyMap.put(typeName, propertiesByName);
                }
                final EventPropertyDescriptor propertyDescriptor =
                    new EventPropertyDescriptor(propertyName, string, real);
                typeDescriptor.getProperties().add(propertyDescriptor);
                propertiesByName.put(propertyName, propertyDescriptor);
            }

            final Result<Record3<String, String, String>> facets = context
                .select(Tables.EVENT_TYPE.EVENT_NAME, Tables.EVENT_PROPERTY.NAME, Tables.EVENT_PROPERTY.VALUE)
                .from(table)
                .groupBy(Tables.EVENT_TYPE.EVENT_NAME, Tables.EVENT_PROPERTY.NAME, Tables.EVENT_PROPERTY.VALUE)
                .orderBy(Tables.EVENT_PROPERTY.VALUE.count())
                .fetch();
            for (final Record3<String, String, String> facet : facets) {
                Map<String, EventPropertyDescriptor> propertiesByName = propertyMap.get(facet.value1());
                final EventPropertyDescriptor property = propertiesByName.get(facet.value2());
                property.getFacets().add(facet.value3());
            }
        }
        return out;
    }

    @Override
    public FacetedTimeSeries executeFacetedQuery(final EventQuery query, final int maxFacetCount)
        throws MonitoringDBException {

        final FacetedTimeSeries out = new FacetedTimeSeries();
        final QueryEntities entities = prepareQueryEntities(query);
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
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
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();

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

    @Override
    public void deleteOldData(final long deleteBeforeEpoch) throws MonitoringDBException {
        try (ConnectionWrapper wrapper = getConnection()) {
            final DSLContext context = wrapper.getContext();
            context.deleteFrom(Tables.EVENT).where(Tables.EVENT.TIME.lessThan(deleteBeforeEpoch)).execute();
            context.deleteFrom(Tables.COUNTER_VALUE).where(Tables.COUNTER_VALUE.TIME.lessThan(deleteBeforeEpoch))
                .execute();
            context.deleteFrom(Tables.METRIC_VALUE).where(Tables.METRIC_VALUE.TIME.lessThan(deleteBeforeEpoch))
                .execute();
        } catch (MonitoringDBException e) {
            throw new MonitoringDBException(e);
        }
    }

    private ConnectionWrapper getConnection() throws MonitoringDBException {
        return connectionProvider.getConnection();
    }

    /**
     * Prepare the QueryEntities structure, used to execute the query and pull back results
     * @param query
     * @return
     */
    private QueryEntities prepareQueryEntities(final EventQuery query) {
        final Short eventType = query.getEventType();
        if (eventType == null) {
            throw new IllegalArgumentException(); // TODO
        }
        final QueryEntities out = new QueryEntities();
        final long endEpoch = query.getWindowEnd() != null ? query.getWindowEnd() : System.currentTimeMillis();
        final long startEpoch = query.getWindowStart() != null ? query.getWindowStart()
            : endEpoch - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
        final int binCount = query.getBinCount();
        out.grouper = new TimeGrouper(startEpoch, endEpoch, binCount);

        out.conditions.add(Tables.EVENT.TIME.between(startEpoch, endEpoch));
        out.conditions.add(Tables.EVENT.EVENT_TYPE_ID.equal(eventType));
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
