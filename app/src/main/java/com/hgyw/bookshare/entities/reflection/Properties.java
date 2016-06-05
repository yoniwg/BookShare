package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by haim7 on 23/03/2016.
 */
public class Properties {

    private static final boolean DEBUG = true;

    /**
     * Creates of all properties of class clazz, where key is the property name.
     * @param clazz
     */
    public static List<Property> getProperties(Class<?> clazz) {
        List<Property> list = new ArrayList<>();
        Method[] methods = clazz.getMethods();
        for (Field field : clazz.getDeclaredFields()) {
            EntityProperty propertyAnnotation = field.getAnnotation(EntityProperty.class);
            if (propertyAnnotation != null && propertyAnnotation.disable()) continue;

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
                        if (setter.getParameterTypes().length != 1) setter = null;
                    }
                    catch (NoSuchMethodException e) { setter = null; }
                    list.add(new ReflectedProperty(field.getName(), getter, setter, field));
                }
            }
        }
        if (clazz.getSuperclass() != null) list.addAll(getProperties(clazz.getSuperclass()));
        return list;
    }

    /***
     * returns stream of properties of aClass to sql
     */
    private static List<Property> getFlatProperties(Class<?> aClass, String subPropertySeparator, Predicate<Class> baseTypePredicate) {
        return Stream.of(Properties.getProperties(aClass))
                .filter(Property::canWrite)
                .flatMap(p -> {
                    if(baseTypePredicate.test(p.getPropertyType())) {
                        return Stream.of(p);
                    } else {
                        List<Property> subProperties = Properties.getProperties(p.getPropertyType());
                        if (subProperties.isEmpty()) {
                            String message = "Flat property error: The type '%s' of property '%s' in class '%s' is not a base-type, and have not sub-properties to reflect.";
                            message = String.format(message, p.getPropertyType(), p.getName(), p.getReflectedClass().getSimpleName());
                            throw new IllegalArgumentException(message);
                        }
                        return Stream.of(subProperties)
                                .map(p2 -> new ConcaveProperties(p, p2, subPropertySeparator));
                    }
                }).collect(Collectors.toList());
    }

    public static List<Property> getFlatProperties(Class<?> aClass, String subPropertySeparator, ConvertersCollection converters) {
        return getFlatProperties(aClass, subPropertySeparator, converters::canConvertFrom);
    }

    public static Property renameProperty(Property p, String newName) {
        return new Property() {
            @Override public void set(Object o, Object value) {p.set(o, value);}
            @Override public Object get(Object o) {return p.get(o);}
            @Override public <T extends Annotation> T getFieldAnnotation(Class<T> annotationClass) {return p.getFieldAnnotation(annotationClass);}
            @Override public String getName() {return newName;}
            @Override public boolean canWrite() {return p.canWrite();}
            @Override public Class<?> getPropertyType() { return p.getPropertyType();}
            @Override public Class<?> getReflectedClass() {return p.getReflectedClass();}
        };
    }

    private static class ConcaveProperties implements Property {
        private final Property p;
        private final Property p2;
        private final String subPropertySeparator;

        private ConcaveProperties(Property p, Property p2, String subPropertySeparator) {
            this.p = p;
            this.p2 = p2;
            this.subPropertySeparator = subPropertySeparator;
        }
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
            return p.getName() + subPropertySeparator + p2.getName();
        }
        public boolean canWrite() {return p.canWrite() && p2.canWrite();}
        public Class<?> getPropertyType() {return p2.getPropertyType();}
        public Class<?> getReflectedClass() {return p.getReflectedClass();}

        public String toString() {
            return "Nested-Property{'" + getName() + "'}";
        }
    }


    public static Property convertProperty(Property p, Converter converter) {
        if (!converter.canConvertFrom(p.getPropertyType())) {
            String message = String.format("The converter (%s) cannot convert from property '%s' (of type %s)", converter, p.getName(), p.getPropertyType());
            throw new IllegalArgumentException(message);
        }
        return new Property() {
            @Override public void set(Object o, Object value) {p.set(o, converter.parse(p.getPropertyType(), value));}
            @Override public Object get(Object o) {return converter.convert(p.get(o));}
            @Override public <T extends Annotation> T getFieldAnnotation(Class<T> annotationClass) {return null;}
            @Override public String getName() {return p.getName();}
            @Override public boolean canWrite() {return p.canWrite();}
            @Override public Class<?> getPropertyType() { return converter.getConvertType();}
            @Override public Class<?> getReflectedClass() {return p.getReflectedClass();}
        };
    }




}
