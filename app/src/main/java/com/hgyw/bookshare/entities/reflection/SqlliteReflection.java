package com.hgyw.bookshare.entities.reflection;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Base64;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.Entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by haim7 on 25/05/2016.
 */
public class SqlLiteReflection  {


    protected static final String SUB_PROPERTY_SEPARATOR = "_";
    public static final String ID_KEY_SQL = "id";

    private final ConvertersCollection sqlLiteConverters = new ConvertersCollection(
            Converters.ofIdentity(Integer.class),
            Converters.ofIdentity(Long.class),
            Converters.ofIdentity(String.class),
            Converters.fullConverter(Boolean.class, Integer.class, b->(b)?1:0, i->i==1),
            Converters.fullConverter(byte[].class, String.class, arr -> Base64.encodeToString(arr, 0), str -> Base64.decode(str,0)), //Converters.ofIdentity(byte[].class),
            Converters.fullConverter(BigDecimal.class, String.class, Object::toString, BigDecimal::new),
            Converters.fullConverterInherit(Date.class, Long.class, Date::getTime, Converters::newDate, type -> Converters.newDate(type, 0)),
            Converters.fullConverterInherit(Enum.class, Integer.class, Enum::ordinal, (type, value) -> type.getEnumConstants()[value])
    );


    public String getSqlLiteNameOf(Class<?> type) {
        type = Converters.toBoxedType(type);
        if (type == Integer.class) return "INTEGER";
        if (type == Long.class) return "INTEGER";
        if (type == Double.class) return "REAL";
        if (type == Float.class) return "REAL";
        if (type == String.class) return "TEXT";
        if (type == byte[].class) return "BLOB";
        throw new IllegalArgumentException("No sqllite type-name for " + type);
    }

    private final Map<Class, Map<String, Property>> propertiesMap = new HashMap<>();

    public Map<String,Property> getProperties(Class aClass) {
        Map<String, Property> properties = propertiesMap.get(aClass);
        if (properties == null) {
            properties = Stream.of(Properties.getFlatProperties(aClass, SUB_PROPERTY_SEPARATOR, sqlLiteConverters::canConvertFrom))
                    .filter(Property::canWrite)
                    .map(p -> Properties.convertProperty(p, sqlLiteConverters.findFullConverter(p.getPropertyType())))
                    .collect(Collectors.toMap(Property::getName, o -> o));
            propertiesMap.put(aClass, properties);
        }
        return properties;
    }

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
        else if (type == Book.class) cv.put(key, (Boolean) value);
        else throw new RuntimeException("No method in " + ContentValues.class + " to put object of " + type);
    }

    private static <T> T genericGet(Cursor cursor, int column, Class<T> type) {
        type = Converters.toUnboxedType(type);
        if (type == int.class) return (T) (Object) cursor.getInt(column);
        if (type == long.class) return (T) (Object) cursor.getLong(column);
        if (type == double.class) return (T) (Object) cursor.getDouble(column);
        if (type == float.class) return (T) (Object) cursor.getFloat(column);
        if (type == String.class) return (T) (Object) cursor.getString(column);
        if (type == boolean.class) return (T) (Object) (!cursor.isNull(column) && cursor.getShort(column) != 0);
        if (type == byte[].class) return (T) (Object) cursor.getBlob(column);
        throw new RuntimeException("No method in " + Cursor.class + " to apply object of " + type);
    }

    public <T> T readObject(Cursor cursor, Class<T> type) {
        T newItem = Converters.tryNewInstanceOrThrow(type);
        Map<String, Property> propertiesMap = getProperties(type);
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            String propertyName = cursor.getColumnName(i);
            Property p = propertiesMap.get(propertyName);
            if (p == null) {
                String message = String.format("No property of cursor column '%s' in properties of %s.", propertyName, type);
                throw new RuntimeException(message);
            }
            Object value = genericGet(cursor, i, p.getPropertyType());
            p.set(newItem, value);
        }
        return newItem;
    }

    public ContentValues writeObject(Object object) {
        ContentValues contentValues = new ContentValues();
        Class type = object.getClass();
        for (Property p : getProperties(type).values()) {
            if (p.getName().equals(ID_KEY_SQL)) continue;
            genericPut(contentValues, p.getName(), p.get(object));
        }
        return contentValues;
    }


    public Stream<Property> streamProperties(Class<? extends Entity> type) {
        return Stream.of(getProperties(type).values());
    }
}
