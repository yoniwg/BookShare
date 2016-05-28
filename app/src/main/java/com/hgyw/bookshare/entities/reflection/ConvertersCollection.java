package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by haim7 on 28/05/2016.
 */
public class ConvertersCollection {
    private final Collection<Converter> converters;

    public ConvertersCollection(Collection<Converter> converters) {
        this.converters = new ArrayList<>(converters);
    }

    public ConvertersCollection(Converter ... converters) {
        this.converters = Arrays.asList(converters.clone());
    }


    public boolean canConvertFrom(Class type) {
        return Stream.of(converters).anyMatch(c -> c.canConvertFrom(type));
    }

    public Converter findConverterOrThrow(Class<?> sourceType) {
        for (Converter c : converters) {
            if (c.canConvertFrom(sourceType))
                 return c;
        }
        throw new IllegalArgumentException("No converter from " + sourceType + ".");
    }

    public Object convert(Object value) {
        return findConverterOrThrow(value.getClass()).convert(value);
    }

    public <T> T parse(Class<T> sourceType, Object value) {
        return (T) findConverterOrThrow(sourceType).parse(sourceType, value);
    }
}
