package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by haim7 on 28/05/2016.
 */
public class ConvertersCollection {
    private final Collection<? extends OneSideConverter> converters;
    private final Collection<? extends Parser> parsers;

    public ConvertersCollection(Collection<OneSideConverter> converters, Collection<Parser> parsers) {
        this.converters = converters == null ? Collections.emptyList() : new ArrayList<>(converters);
        this.parsers = parsers == null ? Collections.emptyList() : new ArrayList<>(parsers);
    }

    public ConvertersCollection(Collection<FullConverter> fullConverters) {
        fullConverters = new ArrayList<>(fullConverters);
        this.converters = fullConverters;
        this.parsers = fullConverters;
    }

    public ConvertersCollection(FullConverter... fullConverters) {
        this(Arrays.asList(fullConverters));
    }

    public boolean canConvertFrom(Class type) {
        return Stream.of(converters).anyMatch(c -> c.canConvertFrom(type));
    }

    /**
     * @throws IllegalArgumentException if converter for sourceType is not fount
     */
    public FullConverter findFullConverter(Class<?> sourceType) {
        for (OneSideConverter c : converters) {
            if (c instanceof FullConverter && c.canConvertFrom(sourceType))
                return (FullConverter) c;
        }
        throw new IllegalArgumentException("No converter from " + sourceType + ".");
    }

    /**
     * @throws IllegalArgumentException if converter for sourceType is not fount
     */
    public OneSideConverter findConverter(Class<?> sourceType) {
        for (OneSideConverter c : converters) {
            if (c.canConvertFrom(sourceType))
                return c;
        }
        throw new IllegalArgumentException("No converter from " + sourceType + ".");
    }

    /**
     * @throws IllegalArgumentException if converter for sourceType is not fount
     */
    public Parser findParser(Class<?> sourceType) {
        for (Parser p : parsers) {
            if (p.canConvertFrom(sourceType))
                return p;
        }
        throw new IllegalArgumentException("No converter from " + sourceType + ".");
    }

    public Object convert(Object value) {
        return findConverter(value.getClass()).convert(value);
    }

    public <T> T parse(Class<T> sourceType, Object value) {
        return (T) findParser(sourceType).parse(sourceType, value);
    }
}
