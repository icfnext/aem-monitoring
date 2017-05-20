package com.icfolson.aem.monitoring.console.util;

import com.icfolson.aem.monitoring.console.model.Operation;
import org.jooq.Condition;
import org.jooq.Field;

public final class JooqUtil {

    public static final Condition getCondition(Field field, Operation operation, Object value) {
        Condition out = null;
        switch (operation) {
        case EQUAL:
            out = field.equal(value);
            break;
        case NOT_EQUAL:
            out = field.notEqual(value);
            break;
        case LESS_THAN:
            out = field.lessThan(value);
            break;
        case LESS_THAN_OR_EQUAL:
            out = field.lessOrEqual(value);
            break;
        case GREATER_THAN:
            out = field.greaterThan(value);
            break;
        case GREATER_THAN_OR_EQUAL:
            out = field.greaterOrEqual(value);
            break;
        case LIKE:
            out = field.like(String.valueOf(value));
            break;
        }
        return out;
    }

    private JooqUtil() { }
}
