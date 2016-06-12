package com.hgyw.bookshare.entities.reflection;

import android.util.Base64;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by haim7 on 27/05/2016.
 */
@Deprecated
public class JsonReflection  {

    public static final String SUB_PROPERTY_SEPARATOR = "_";
    private static final String ID_KEY = "id";

    private final ConvertersCollection jsonConverters = new ConvertersCollection(
            Converters.ofIdentity(String.class),
            Converters.ofIdentity(Integer.class),
            Converters.ofIdentity(Long.class),
            Converters.ofIdentity(Double.class),
            Converters.fullConverter(Boolean.class, Integer.class, b->(b)?1:0, i->i==1),
            Converters.fullConverter(byte[].class, String.class, arr -> Base64.encodeToString(arr, 0), str -> Base64.decode(str,0)),
            Converters.fullConverter(BigDecimal.class, String.class, Object::toString, BigDecimal::new, BigDecimal.ZERO),
            Converters.fullConverterInherit(Date.class, Long.class, Date::getTime, Converters::newInstance, Converters::newInstance),
            Converters.fullConverterInherit(Enum.class, Integer.class, Enum::ordinal, (type, i) -> type.getEnumConstants()[i], type -> type.getEnumConstants()[0])
    );

    private final Map<Class, Map<String, Property>> propertiesMap = new HashMap<>();

    public Map<String,Property> getProperties(Class aClass) {
        Map<String, Property> properties = propertiesMap.get(aClass);
        if (properties == null) {
            properties = Stream.of(Properties.getFlatProperties(aClass, SUB_PROPERTY_SEPARATOR, jsonConverters::canConvertFrom))
                    .filter(Property::canWrite)
                    .map(p -> Properties.convertProperty(p, jsonConverters.findFullConverter(p.getPropertyType())))
                    .collect(Collectors.toMap(Property::getName, o -> o));
            propertiesMap.put(aClass, properties);
        }
        return properties;
    }

    public <T> T readObject(Class<T> type, JSONObject jsonObject) {
        T item = Converters.newInstance(type);
        for (Property p : getProperties(type).values()) {
            Object jsonValue;
            try {
                jsonValue = getFromJson(jsonObject, p.getPropertyType(), p.getName());
                if (jsonValue.equals(JSONObject.NULL)) jsonValue = null;
            }
            catch (JSONException e) {throw new RuntimeException(e);}
            p.set(item, jsonValue);
        }
        return item;
    }

    private Object getFromJson(JSONObject jsonObject, Class<?> propertyType, String key) throws JSONException {
        if (propertyType == Integer.class) {
            return jsonObject.getInt(key);
        } else if (propertyType == Long.class) {
            return jsonObject.getLong(key);
        } else if (propertyType == Boolean.class) {
            return jsonObject.getBoolean(key);
        } else if (propertyType == Double.class) {
            return jsonObject.getDouble(key);
        } else if (propertyType == String.class) {
            return jsonObject.getString(key);
        } else {
            throw new RuntimeException("Cannot convert from jsonObject to " + propertyType.getName());
        }
    }

    public JSONObject writeObject(Object item) {
        JSONObject jsonObject = new JSONObject();
        Class type = item.getClass();
        for (Property p : getProperties(type).values()) {
            if (p.getName().equals(ID_KEY)) continue;
            Object jsonValue = p.get(item);
            try {
                jsonObject.put(p.getName(), jsonValue);
            } catch (JSONException e) {throw new RuntimeException(e);}
        }
        return jsonObject;
    }

    public ConvertersCollection converters() {
        return jsonConverters;
    }
}
