package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Stream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by haim7 on 23/03/2016.
 */
public class PropertiesReflection {

    /**
     * Creates of all properties of class clazz, where key is the property name.
     * @param clazz
     */
    public static List<Property> getProperties(Class<?> clazz) {
        List<Property> list = new ArrayList<>();
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

    public interface PropertiesConvertManager {
        Stream<Property> streamFlatProperties(Class aClass);
        List<Converters.Converter> getConverters();
        void setConverters(List<Converters.Converter> converter);
        Converters.Converter findConverter(Class aClass);
        boolean isConverterExists(Class aClass);
    }


    public static PropertiesConvertManager newPropertiesConvertManager(String subPropertySeparator, List<Converters.Converter> converters) {
        PropertiesConvertManager propertiesConvertManager = newPropertiesConvertManager(subPropertySeparator);
        propertiesConvertManager.setConverters(converters);
        return propertiesConvertManager;
    }

    public static PropertiesConvertManager newPropertiesConvertManager(String subPropertySeparator) {
        return new PropertiesConvertManager() {
            private final Map<Class, Property[]> flatPropertiesMap = new HashMap<>();
            private List<Converters.Converter> converters = new ArrayList<>();

            public List<Converters.Converter> getConverters() {
                return converters;
            }

            @Override
            public void setConverters(List<Converters.Converter> converters) {
                this.converters = Objects.requireNonNull(converters);
            }

            /***
             * returns stream of propertios of aClass to sql
             */
            private Stream<Property> generateFlatProperties(Class aClass) {
                return Stream.of(PropertiesReflection.getProperties(aClass))
                        .filter(Property::canWrite)
                        .flatMap(p -> {
                            if(isConverterExists(p.getPropertyType())) {
                                return Stream.of(p);
                            } else {
                                return Stream.of(PropertiesReflection.getProperties(p.getPropertyType()))
                                        .map(p2 -> new ConcaveProperties(p, p2, subPropertySeparator));
                            }
                        });
            }

            public Stream<Property> streamFlatProperties(Class aClass) {
                Property[] props = flatPropertiesMap.get(aClass);
                if (props == null) {
                    props = generateFlatProperties(aClass).toArray(Property[]::new);
                    flatPropertiesMap.put(aClass, props);
                }
                return Stream.of(props);
            }

            public Converters.Converter findConverter(Class type) {
                type = Converters.toBoxedType(type);
                for (Converters.Converter converter : this.converters) if (converter.getType() == type) return converter;
                if (type.isEnum()) {
                    return Converters.enumToIntegerConverter(type, "INTEGER");
                }
                throw new IllegalArgumentException("No sqlLite-Converter to " + type + ".");
            }

            public boolean isConverterExists(Class type) {
                try { findConverter(type); return true;}
                catch (IllegalArgumentException e) {return false;}
            }

        };
    }

    ;;;;
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
        public boolean canWrite() {return p2.canWrite();}
        public Class<?> getPropertyType() {return p2.getPropertyType();}
        public Class<?> getReflectedClass() {return p.getReflectedClass();}
        public String toString() {
            return "Nested-Property{'" + getName() + "'}";
        }
    }

    //////////////////////////////////
    // Properties flating
    //////////////////////////////////


}
