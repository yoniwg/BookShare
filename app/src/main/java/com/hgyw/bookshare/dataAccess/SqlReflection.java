package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.reflection.PropertiesReflection;
import com.hgyw.bookshare.entities.reflection.Property;

import java.lang.annotation.Annotation;

/**
 * Created by haim7 on 24/05/2016.
 */
public class SqlReflection {

    private static final String SUB_PROPERTY_SEP = "_0";

    private static String classToSqlType(Class aClass) {
        return "";
    }

    private static String convertToSqlValue(Object o) {
        return null;
    }

    private static Object convertFromSqlValue(String o) {
        return null;
    }

    public static Stream<Property> streamFlatProperties(Class aClass) {
        return Stream.of(PropertiesReflection.getPropertiesMap(aClass).values())
                .flatMap(p -> {
                    boolean isSqlType = classToSqlType(p.getPropertyClass()).isEmpty();
                    return isSqlType ? Stream.of(p) : flatProperties(p);
                });
    }

    // set all properties of property-class to property of reflected-class with
    //  name 'outer-property'+'inner-property'
    private static Stream<Property> flatProperties(Property p) {
        return streamFlatProperties(p.getPropertyClass())
                .map(p2 -> new Property() {
                    public void set(Object o, Object value) {
                        p2.set(p.get(o), value);
                    }
                    public Object get(Object o) {
                        return p2.get(p.get(o));
                    }
                    public <T extends Annotation> T getFieldAnnotation(Class<T> annotationClass) {
                        return p2.getFieldAnnotation(annotationClass);
                    }
                    public String getName() {
                        return p.getName() + SUB_PROPERTY_SEP + p2.getName();
                    }
                    public boolean canWrite() {return p2.canWrite();}
                    public Class<?> getPropertyClass() {return p2.getPropertyClass();}
                    public Class<?> getReflectedClass() {return p.getReflectedClass();}
                });
    }

    private static Stream<Property> streamProperties2(Class aClass) {
        return Stream.of(PropertiesReflection.getPropertiesMap(aClass).values())
                .filter(p -> !classToSqlType(p.getPropertyClass()).isEmpty());
    }

    private static String sqlTable(Entity item) {
        return sqlTable(item.getClass());
    }

    private static String sqlTable(Class aClass) {
        return aClass.getSimpleName();
    }

    public static String sqlValues(Entity item) {
        String sqlValues = streamFlatProperties(item.getClass())
                .map(p -> convertToSqlValue(p.get(item)))
                .collect(Collectors.joining(","));
        return sqlValues;
    }

    public static String sqlColumnType(Class aClass) {
        String sqlValues = streamFlatProperties(aClass)
                .map(p -> p.getName() + " " + classToSqlType(p.getPropertyClass()))
                .collect(Collectors.joining(","));
        return sqlValues;
    }

    public static String sqlColumnValue(Entity item) {
        String sqlValues = streamFlatProperties(item.getClass())
                .map(p -> p.getName() +"=" + convertToSqlValue(p.get(item)))
                .collect(Collectors.joining(","));
        return sqlValues;
    }

}
