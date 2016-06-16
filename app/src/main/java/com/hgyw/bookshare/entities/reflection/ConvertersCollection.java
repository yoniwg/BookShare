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
    private final Collection<? extends FullConverter> fullConverters;

    public ConvertersCollection(Collection<FullConverter> fullConverters) {
        fullConverters = new ArrayList<>(fullConverters);
        this.fullConverters = fullConverters;
    }

    public ConvertersCollection(FullConverter... fullConverters) {
        this(Arrays.asList(fullConverters));
    }

    public boolean canConvertFrom(Class<?> sourceType) {
        return Stream.of(fullConverters).anyMatch(c -> c.canConvertFrom(sourceType));
    }


    /**
     * @throws IllegalArgumentException if converter for sourceType is not fount
     */
    public FullConverter findFullConverter(Class<?> sourceType) {
        for (FullConverter c : fullConverters) {
            if (c.canConvertFrom(sourceType))
                return c;
        }
        throw new IllegalArgumentException("No converter from " + sourceType + ".");
    }

    public Object convert(Object value) {
        return findFullConverter(value.getClass()).convert(value);
    }

    public <T> T parse(Class<T> sourceType, Object value) {
        return (T) findFullConverter(sourceType).parse(sourceType, value);
    }
}
