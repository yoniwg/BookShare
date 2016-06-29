package com.hgyw.bookshare.entities.reflection;

/**
 * Created by haim7 on 04/05/2016.
 */

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Class represents property, that accepts getter and setter of java.reflection, and property name.
 * The setter can be null and then the property will be read-only.
 * It can also get field for getting annotation.
 */
class ReflectedProperty implements Property {
    private final String name;
    private final Method setter;
    private final Method getter;
    private final Field field;

    public ReflectedProperty(String name, Method getter, Method setter, Field field) {
        Objects.requireNonNull(getter);
        if (!Modifier.isPublic(getter.getModifiers()) || setter != null && !Modifier.isPublic(setter.getModifiers())) {
            throw new IllegalArgumentException("The getter and setter should be public. field: " + field);
        }
        if (getter.getParameterTypes().length != 0) {
            throw new IllegalArgumentException("The getter should not have parameters. field: " + field);
        }
        if (setter != null) {
            if (setter.getParameterTypes().length != 1 || setter.getParameterTypes()[0] != getter.getReturnType()) {
                throw new IllegalArgumentException("The setter should have only one parameter with type equals to returned type of getter. field: " + field);
            }
        }
        if (field != null && field.getType() != getter.getReturnType()) throw new IllegalArgumentException("The getter and field should be the same type. field: " + field);
        this.name = Objects.requireNonNull(name); this.setter = setter; this.getter = getter; this.field = field;
    }

    @Override
    public void set(Object o, Object value) {
        if (setter == null) throw new UnsupportedOperationException("The Property '" + name + "' is read only.");
        try {
            setter.invoke(o, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // unreached code because the method is public
        }
    }

    @Override
    public Object get(Object o) {
        try {
            return getter.invoke(o);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new InternalError(e.getMessage()); // unreached code because the method is public
        }
    }

    @Override
    public <T extends Annotation> T getFieldAnnotation(Class<T> annotationClass) {
        return field == null ? null : field.getAnnotation(annotationClass);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canWrite() {
        return setter != null;
    }

    @Override
    public Class<?> getPropertyType() {
        return getter.getReturnType();
    }

    @Override
    public Class<?> getReflectedClass() {
        return getter.getDeclaringClass();
    }

    @Override
    public String toString() {
        return "ReflectedProperty{" +
                '\'' + name + '\'' +
                (setter == null ? " No-setter" : "") +
                '}';
    }
}

