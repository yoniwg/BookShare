package com.hgyw.bookshare.entities.reflection;

import android.content.ContentValues;
import android.database.Cursor;

import com.annimon.stream.Collectors;
import com.hgyw.bookshare.entities.reflection.PropertiesReflection.PropertiesConvertManager;

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
        Class type = Converters.toBoxedType(value.getClass());
        if (type == Integer.class) cv.put(key, (Integer) value);
        else if (type == Long.class) cv.put(key, (Long) value);
        else if (type == Double.class) cv.put(key, (Double) value);
        else if (type == Float.class) cv.put(key, (Float) value);
        else if (type == String.class) cv.put(key, (String) value);
        else if (type == byte[].class) cv.put(key, (byte[]) value);
        else throw new RuntimeException("No method in " + ContentValues.class + " to put object of " + type);
    }

        private static <T> T genericGet(Cursor cursor, int column, Class<T> type) {
        type = Converters.toUnboxedType(type);
        if (type == int.class) return (T) (Object) cursor.getInt(column);
        if (type == long.class) return (T) (Object) cursor.getLong(column);
        if (type == double.class) return (T) (Object) cursor.getDouble(column);
        if (type == float.class) return (T) (Object) cursor.getFloat(column);
        if (type == String.class) return (T) (Object) cursor.getString(column);
        if (type == byte[].class) return (T) (Object) cursor.getBlob(column);
        throw new RuntimeException("No method in " + Cursor.class + " to get object of " + type);
    }

    public static <T> T readObject(Cursor cursor, Class<T> type, PropertiesConvertManager propertiesConvertManager) {
        T newItem = Converters.tryNewInstanceOrThrow(type);
        Map<String, Property> propertiesMap = propertiesConvertManager.streamFlatProperties(type)
                .collect(Collectors.toMap(Property::getName, o -> o));
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            String propertyName = cursor.getColumnName(i);
            Property p = propertiesMap.get(propertyName);
            if (p == null) {
                String message = "No property of cursor column '" + propertyName + "'.";
                throw new RuntimeException(message);
            }
            Converters.Converter converter = propertiesConvertManager.findConverter(p.getPropertyType());
            Object convertValue = genericGet(cursor, i, converter.getConvertType());
            Object value = converter.parse(convertValue);
            p.set(newItem, value);
        }
        return newItem;
    }

    public static ContentValues writeObject(Object object, PropertiesConvertManager propertiesConvertManager) {
        ContentValues contentValues = new ContentValues();
        Class type = object.getClass();
        propertiesConvertManager.streamFlatProperties(type)
                .filter(p -> !p.getName().equalsIgnoreCase("id"))
                .forEach(p -> {
                    Object value = p.get(object);
                    Object convertValue = propertiesConvertManager.findConverter(p.getPropertyType()).convert(value);
                    genericPut(contentValues, p.getName(), convertValue);
                });
        return contentValues;
    }


}
