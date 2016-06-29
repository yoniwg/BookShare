package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Collection of converter with methods for finding converter for a type.
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

    public boolean hasConverterFrom(Class<?> sourceType) {
        for (FullConverter c : fullConverters) {
            if (c.canConvertFrom(sourceType))
                return true;
        }
        return false;
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

}
