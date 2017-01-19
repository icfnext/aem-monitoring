package com.icfolson.aem.monitoring.core.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@JsonDeserialize(using = QualifiedName.Deserializer.class)
@JsonSerialize(using = QualifiedName.Serializer.class)
public class QualifiedName {

    private final String[] elements;

    public QualifiedName(final String ... elements) {
        this.elements = Arrays.copyOf(elements, elements.length);
    }

    public String[] getElements() {
        return Arrays.copyOf(elements, elements.length);
    }

    public QualifiedName getParent() {
        if (elements.length == 0) {
            return null;
        }
        return new QualifiedName(Arrays.copyOf(elements, elements.length - 1));
    }

    public QualifiedName getChild(final String name) {
        final String[] names = Arrays.copyOf(elements, elements.length + 1);
        names[elements.length] = name;
        return new QualifiedName(names);
    }

    public int getLength() {
        return elements.length;
    }

    public String getElement(int index) {
        return elements[index];
    }

    public String getJoined(final char joinCharacter) {
        return Joiner.on(joinCharacter).join(elements);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final QualifiedName that = (QualifiedName) o;

        return Arrays.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(elements);
    }

    public static class Deserializer extends StdDeserializer<QualifiedName> {

        private static final Splitter SPLITTER = Splitter.on('.');

        protected Deserializer() {
            super(QualifiedName.class);
        }

        @Override
        public QualifiedName deserialize(final JsonParser jsonParser,
            final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            final String string = jsonParser.getValueAsString();
            final List<String> components = SPLITTER.splitToList(string);
            final String[] array = components.toArray(new String[components.size()]);
            return new QualifiedName(array);
        }
    }

    public static class Serializer extends StdSerializer<QualifiedName> {

        private static final Joiner JOINER = Joiner.on('.');

        protected Serializer() {
            super(QualifiedName.class);
        }

        @Override
        public void serialize(final QualifiedName qualifiedName, final JsonGenerator jsonGenerator,
            final SerializerProvider serializerProvider) throws IOException {
            final String string = JOINER.join(qualifiedName.elements);
            jsonGenerator.writeString(string);
        }
    }
}
