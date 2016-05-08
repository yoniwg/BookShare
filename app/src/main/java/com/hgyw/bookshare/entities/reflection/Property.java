package hgyw.com.bookshare.entities.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

/**
 * Interface for property
 */
public interface Property {
    // throws IllegalArgumentException, UnsupportedOperationException if read-only
    void set(Object o, Object value);
    // throws IllegalArgumentException
    Object get(Object o);

    <T extends Annotation> T getFieldAnnotation(Class<T> annotationClass);

    String getName();
    boolean canWrite();
    Class<?> getPropertyClass();
    Class<?> getReflectedClass();
    Annotation[] getFieldAnnotations();
}
