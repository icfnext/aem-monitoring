package com.icfolson.aem.monitoring.core.util;

import com.google.common.base.Splitter;
import com.icfolson.aem.monitoring.core.model.QualifiedName;

import java.util.ArrayList;
import java.util.List;

public final class NameUtil {

    public static final char DIVIDER = '\u241D';

    public static QualifiedName toName(final String storageFormat) {
        final Iterable<String> split = Splitter.on(DIVIDER).split(storageFormat);
        List<String> hierarchy = new ArrayList<>();
        for (final String s : split) {
            hierarchy.add(s);
        }
        return new QualifiedName(hierarchy.toArray(new String[hierarchy.size()]));
    }

    public static String toStorageFormat(final QualifiedName name) {
        return name.getJoined(DIVIDER);
    }

    private NameUtil() { }

}
