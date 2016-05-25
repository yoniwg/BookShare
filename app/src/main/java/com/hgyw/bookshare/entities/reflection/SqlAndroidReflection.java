package com.hgyw.bookshare.entities.reflection;

import android.content.ContentValues;
import android.database.Cursor;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.dataAccess.SqlReflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Created by haim7 on 25/05/2016.
 */
public class SqlAndroidReflection {

    /**
     * if value == null do nothing
     */
    private static void genericPut(ContentValues cv, String key, Object value) {
        if (value == null) return;
        try {
            Method putMethod = cv.getClass().getMethod("put", String.class, value.getClass());
            if (!Modifier.isPublic(putMethod.getModifiers())) {
                String message = String.format("The class %s has no public method put(String, %s).", cv.getClass().getName(), value.getClass().getName());
                throw new IllegalArgumentException(message);
            }
            putMethod.invoke(cv, key, value);
        } catch (NoSuchMethodException e) {
            String message = String.format("The class %s has no method put(String, %s).", cv.getClass().getName(), value.getClass().getName());
            throw new IllegalArgumentException(message);
        } catch (InvocationTargetException | IllegalAccessException ignored) {
            // should not be occurred
        }
    }

    private static <T> T genericGet(Cursor cursor, int column, Class<T> type) {
        try {
            type = (Class<T>) type.getField("TYPE").get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        String typeName = type.getSimpleName();
        String methodName = "get" + Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
        try {
            return (T) Cursor.class.getMethod(methodName, int.class).invoke(cursor, column);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("No method in " + Cursor.class + " of " + methodName + "(int).");
        }
    }

    public static <T> T readObject(Cursor cursor, Class<T> type) {
        T newItem;
        try {
            newItem = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("The class should produce a public empty constructor.", e);
        }
        Map<String, Property> propertiesMap = SqlReflection.streamFlatProperties(type)
                .collect(Collectors.toMap(Property::getName, o -> o));
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            String propertyName = cursor.getColumnName(i);
            Property p = propertiesMap.get(propertyName);
            if (p == null) {
                String message = "No property of cursor column '" + propertyName + "'.";
                throw new RuntimeException(message);
            }
            Converters.Converter converter = SqlReflection.sqlLiteConverterOf(p.getPropertyType());
            Object convertValue = genericGet(cursor, i, converter.getConvertType());
            Object value = converter.parse(convertValue);
            p.set(newItem, value);
        }
        return newItem;
    }

    public static void writeObject(ContentValues contentValues, Object object) {
        Class type = object.getClass();
        SqlReflection.streamFlatProperties(type)
                .filter(p -> !p.getName().equalsIgnoreCase("id"))
                .forEach(p -> {
                    Object value = p.get(object);
                    Object convertValue = SqlReflection.sqlLiteConverterOf(p.getPropertyType()).convert(value);
                    genericPut(contentValues, p.getName(), convertValue);
                });
    }


}
