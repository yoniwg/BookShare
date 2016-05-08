package hgyw.com.bookshare.entities.reflection;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import hgyw.com.bookshare.entities.Entity;
import hgyw.com.bookshare.entities.EntityReference;
import hgyw.com.bookshare.entities.IdReference;

/**
 * Created by haim7 on 23/03/2016.
 */
public class PropertiesReflection {

    /**
     * Creates of all properties of class clazz, where key is the property name.
     * @param clazz
     */
    public static Map<String, Property> getPropertiesMap(Class<?> clazz) {
        Map<String, Property> map = new HashMap<>();
        Method[] methods = clazz.getMethods();
        for (Field field : clazz.getDeclaredFields()) {
            for (Method getter : methods) {
                String getterName = getter.getName();
                String prefix;
                String nameUppercase;
                if (getter.getParameterTypes().length == 0
                        && (getterName.startsWith(prefix = "get") || getter.getReturnType() == boolean.class && getterName.startsWith(prefix = "is"))
                        && !Character.isLowerCase(getterName.charAt(prefix.length()))
                        && field.getName().equalsIgnoreCase(nameUppercase = getter.getName().substring(prefix.length())))
                {
                    Method setter;
                    try {
                        setter = clazz.getMethod("set" + nameUppercase, getter.getReturnType());
                        if (getter.getParameterTypes().length != 1) setter = null;
                    }
                    catch (NoSuchMethodException e) { setter = null; }
                    map.put(field.getName(), new ReflectedProperty(field.getName(), getter, setter, field));
                }
            }
        }
        if (clazz.getSuperclass() != null) map.putAll(getPropertiesMap(clazz.getSuperclass()));
        return map;
    }

}
