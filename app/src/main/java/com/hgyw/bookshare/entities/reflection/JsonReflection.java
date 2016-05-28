package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Entity;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by haim7 on 27/05/2016.
 */
public class JsonReflection  {

    public static final String SUB_PROPERTY_SEPARATOR = "_";

    private final ConvertersCollection jsonConverters = new ConvertersCollection(
            Converters.ofIdentity(String.class),
            Converters.ofIdentity(Integer.class),
            Converters.ofIdentity(Long.class),
            Converters.ofIdentity(Double.class),
            Converters.ofIdentity(Boolean.class),
            Converters.simple(byte[].class, String.class, arr -> "", str -> new byte[0]),
            Converters.simple(BigDecimal.class, String.class, Object::toString, BigDecimal::new),
            Converters.simple(Date.class, Long.class, Date::getTime, Date::new),
            Converters.simple(java.sql.Date.class, Long.class, Date::getTime, java.sql.Date::new)
    );

    private final Map<Class, Map<String, Property>> propertiesMap = new HashMap<>();

    private Map<String,Property> getProperties(Class aClass) {
        Map<String, Property> properties = propertiesMap.get(aClass);
        if (properties == null) {
            properties = Stream.of(Properties.getFlatProperties(aClass, SUB_PROPERTY_SEPARATOR, jsonConverters))
                    .filter(Property::canWrite)
                    .map(p -> Properties.convertedProperty(p, jsonConverters.findConverterOrThrow(p.getPropertyType())))
                    .collect(Collectors.toMap(Property::getName, o -> o));
            propertiesMap.put(aClass, properties);
        }
        return properties;
    }



    public <T extends Entity> T readObject(Class<T> type, JSONObject jsonObject) {
        return Converters.tryNewInstanceOrThrow(type);
    }
}
