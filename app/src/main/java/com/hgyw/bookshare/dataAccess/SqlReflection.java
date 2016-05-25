package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.reflection.Converters;
import com.hgyw.bookshare.entities.reflection.Converters.Converter;
import com.hgyw.bookshare.entities.reflection.PropertiesReflection;
import com.hgyw.bookshare.entities.reflection.Property;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by haim7 on 24/05/2016.
 */
public class SqlReflection {

    private static final String SUB_PROPERTY_SEPARATOR = "__";

    private static List<Converter> sqlLiteConverter = Arrays.asList(new Converter[]{
            Converters.ofIdentity(Integer.class, "INTEGER"),
            Converters.ofIdentity(Long.class, "BIGINT"),
            Converters.ofIdentity(String.class, "TEXT"),
            Converters.simple(BigDecimal.class, String.class, Object::toString, BigDecimal::new, "TEXT"),
            Converters.simple(Date.class, Long.class, Date::getTime, Date::new, "BIGINT"),
            Converters.simple(java.sql.Date.class, Long.class, Date::getTime, java.sql.Date::new, "BIGINT"),
    });

    private static <T extends Enum<T>> Converter<T,Integer> enumSqlLiteConverter(Class<T> type) {
        return Converters.simple(type, Integer.class, Enum::ordinal, i -> type.getEnumConstants()[i], "INTEGER");
    }

    public static Converter sqlLiteConverterOf(Class type) {
        type = Converters.toBoxedType(type);
        for (Converter converter : sqlLiteConverter) if (converter.getType() == type) return converter;
        if (type.isEnum()) {
            return enumSqlLiteConverter(type);
        }
        throw new IllegalArgumentException("No sqlLite-Converter to " + type + ".");
    }

    private static boolean sqlLiteConverterExistsFor(Class<?> type) {
        try {
            sqlLiteConverterOf(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    //////////////////////////////////
    // Properties flating
    //////////////////////////////////

    private static final Map<Class,Property[]> flatPropertiesMap = new HashMap<>();

    /***
     * returns stream of propertios of aClass to sql
     */
    private static Stream<Property> generateFlatProperties(Class aClass) {
        return Stream.of(PropertiesReflection.getProperties(aClass))
                .filter(Property::canWrite)
                .flatMap(p -> {
                    boolean isSqlType = sqlLiteConverterExistsFor(p.getPropertyType());
                    return isSqlType ? Stream.of(p) : flatProperty(p);
                });
    }

    public static Stream<Property> streamFlatProperties(Class aClass) {
        Property[] props = flatPropertiesMap.get(aClass);
        if (props == null) {
            props = generateFlatProperties(aClass).toArray(Property[]::new);
            flatPropertiesMap.put(aClass, props);
        }
        return Stream.of(props);

    }


    // Set all properties of property-class to property of reflected-class with
    //  name 'outer-property'+'inner-property'
    private static Stream<Property> flatProperty(Property p) {
        return generateFlatProperties(p.getPropertyType())
                .map(p2 -> new Property() {
                    public void set(Object o, Object value) {
                        Object pObject = p.get(o);
                        if (pObject == null) {
                            try {
                                p.set(o, pObject = p.getPropertyType().newInstance());
                            } catch (InstantiationException | IllegalAccessException e) {
                                String message = "Cannot set property '{0}.{1}', because the property '{1}' is null and has not public default constructor";
                                message = MessageFormat.format(message, p.getName(), p2.getName());
                                throw new IllegalArgumentException(message);
                            }
                        }
                        p2.set(pObject, value);
                    }
                    public Object get(Object o) {
                        Object pObject = p.get(o);
                        if (pObject == null) { return null; }
                        return p2.get(p.get(o));
                    }
                    public <T extends Annotation> T getFieldAnnotation(Class<T> annotationClass) {
                        return p2.getFieldAnnotation(annotationClass);
                    }
                    public String getName() {
                        return p.getName() + SUB_PROPERTY_SEPARATOR + p2.getName();
                    }
                    public boolean canWrite() {return p2.canWrite();}
                    public Class<?> getPropertyType() {return p2.getPropertyType();}
                    public Class<?> getReflectedClass() {return p.getReflectedClass();}
                    public String toString() {
                        return "Nested-Property{'" + getName() + "'}";
                    }
                });
    }


    //////////////////////////
    // sql data
    /////////////////////////

    /*public static String sqlValues(Object item) {
        return streamFlatProperties(item.getClass())
                .map(p -> objectAsSqlValue(p.get(item)))
                .collect(Collectors.joining(","));
    }

    public static String sqlColumnType(Class aClass) {
        return streamFlatProperties(aClass)
                .map(p -> p.getName() + " " + sqlLiteConverterOf(p.getPropertyType()).getSqlTypeName())
                .collect(Collectors.joining(","));
    }

    public static String sqlColumnValue(Object item) {
        return streamFlatProperties(item.getClass())
                .map(p -> {
                    return p.getName() + "=" + objectAsSqlValue(p.get(item));
                })
                .collect(Collectors.joining(","));
    }*/


}
