package com.hgyw.bookshare.entities.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

/**
 * Interface for property
 */
public interface Property {
    /*
     * Set value to object holds the property
     * @throws IllegalArgumentException if o is not instance of reflectedClass.
     * @throws UnsupportedOperationException if canWrite() == false
     */
    void set(Object o, Object value);

    /**
     * Get value from object holds the property
     * @throws IllegalArgumentException if o is not instance of reflectedClass.
     */
    Object get(Object o);

    /**
     * Get annotation of field associated with property.
     * @return Annotation object, or null if the property is not associated with the annotation class.
     */
    <T extends Annotation> T getFieldAnnotation(Class<T> annotationClass);

    String getName();

    boolean canWrite();

    Class<?> getPropertyType();

    /**
     * @return the class of object holds the property
     */
    Class<?> getReflectedClass();

}
