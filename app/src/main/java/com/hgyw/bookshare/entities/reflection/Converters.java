package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.BiFunction;
import com.annimon.stream.function.Function;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * Static class for create converters and related methods.
 */
public final class Converters {

    private Converters() {} // restrict instantiation

    ///////////////////////////////
    // Related methods
    ///////////////////////////////

    /**
     * invoke constructor match the params. Boxed types will be as unboxed.
     * @throws NullPointerException if aClass is null, or any of params is null.
     * @throws IllegalArgumentException if any problem occur in retrieve constructor and invoke it.
     */
    public static <T> T newInstance(Class<T> aClass, Object ... params) {
        aClass = toBoxedType(aClass);
        Class<?>[] paramsTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param == null) throw new NullPointerException("The params should not be null. param " + i + " is null.");
            paramsTypes[i] = toUnboxedType(params[i].getClass());
        }
        try {
            return aClass.getConstructor(paramsTypes).newInstance(params);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            String message = "Cannot create new instance of " + aClass.getName() + " with constructor new(" +
                    Stream.of(paramsTypes).map(Class::getName).collect(Collectors.joining(",")) + ")";
            throw new IllegalArgumentException(message, e);
        }
    }

    /**
     * @throws NullPointerException if type == null
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> toBoxedType(Class<T> type) {
        if (!type.isPrimitive()) return type;
        Class c;
        if (type == Integer.TYPE) c = Integer.class;
        else if (type == Long.TYPE) c = Long.class;
        else if (type == Boolean.TYPE) c = Boolean.class;
        else if (type == Double.TYPE) c = Double.class;
        else if (type == Float.TYPE) c = Float.class;
        else {
            throw new UnsupportedOperationException("The convert to boxed type from " + type + " is not implemented.");
        }
        return c;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> toUnboxedType(Class<T> type) {
        if (type.isPrimitive()) return type;
        try {
            Class unboxed = (Class) type.getField("TYPE").get(null);
            if (unboxed.isPrimitive()) type = unboxed;
        } catch (IllegalAccessException | NoSuchFieldException ignored) {}
        return type;
    }

    ///////////////////////////////
    // Converters methods
    ///////////////////////////////


    private static abstract class AbstractFullConverter<T,ConvertT> implements FullConverter<T,ConvertT> {
        private String sqlTypeName = "";
        @Override public String getSqlType() {return sqlTypeName;}
        @Override
        public FullConverter<T, ConvertT> withSqlType(String sqlTypeName) {
            this.sqlTypeName = sqlTypeName == null ? "" : sqlTypeName;
            return this;
        }
        protected void requierCanParseTo(Class type) {
            if (!canConvertFrom(type)) throw new IllegalArgumentException("This converter cannot cast to " + type);
        }

        @Override
        public <T2> FullConverter<T2,ConvertT> subConverter(FullConverter<T2,T> subFullConverter) {
            return new AbstractFullConverter<T2, ConvertT>() {
                @Override public Class<T2> getType() {return subFullConverter.getType();}
                @Override public boolean canConvertFrom(Class type) {return toBoxedType(type) == getType();}
                @Override public Class<ConvertT> getConvertType() { return AbstractFullConverter.this.getConvertType(); }
                @Override public ConvertT convert(T2 value) { return AbstractFullConverter.this.convert(subFullConverter.convert(value)); }
                @Override public <R extends T2> R parse(Class<R> type, ConvertT value) {
                    return subFullConverter.parse(type, AbstractFullConverter.this.parse(subFullConverter.getConvertType(), value));
                }
            };
        }

        @Override public String toString() {
            return String.format("%s(from %s to %s)", this.getClass().getSimpleName(), getType().getSimpleName(), getConvertType().getSimpleName());
        }

    }

    public static <T> FullConverter<T,T> ofIdentity(Class<T> type) {
        return new AbstractFullConverter<T, T>()  {
            @Override public Class<T> getType() {return type;}
            @Override public boolean canConvertFrom(Class type) {return toBoxedType(type) == getType();}
            @Override public Class<T> getConvertType() {return type;}
            @Override public T convert(T value) {return value;}
            @Override public <R extends T> R parse(Class<R> type, T value) {
                requierCanParseTo(type);
                return (R) value;
            }
        };
    }

    public static <T,ConvertT> FullConverter<T,ConvertT> fullConverter(Class<T> type,
                                                                       Class<ConvertT> convertType,
                                                                       Function<T, ConvertT> convertFunction,
                                                                       Function<ConvertT, T> parseFunc,
                                                                       T defaultValue) {
        return new AbstractFullConverter<T, ConvertT>() {
            @Override public Class<T> getType() {return type;}
            @Override public boolean canConvertFrom(Class type) {return toBoxedType(type) == getType();}
            @Override public Class<ConvertT> getConvertType() {return convertType;}
            @Override public ConvertT convert(T value) {return convertFunction.apply(value == null ? defaultValue : value);}
            @Override public <R extends T> R parse(Class<R> type, ConvertT value) {
                requierCanParseTo(type);
                return (R) (value == null ? defaultValue : parseFunc.apply(value));
            }
        };
    }

    /**
     * Same as {@link Converters#fullConverter} above, but with default value null.
     */
    public static <T,ConvertT> FullConverter<T,ConvertT> fullConverter(Class<T> type,
                                                                       Class<ConvertT> convertType,
                                                                       Function<T, ConvertT> convertFunction,
                                                                       Function<ConvertT, T> parseFunc) {
        return fullConverter(type, convertType, convertFunction, parseFunc, null);
    }

    /**
     * Converter that depend on the class which into you want to parse
     */
    public static <T,ConvertT> FullConverter<T,ConvertT> fullConverterInherit(Class<T> type,
                                                                              Class<ConvertT> convertType,
                                                                              Function<T, ConvertT> convertFunction,
                                                                              BiFunction<Class<? extends T>, ConvertT, T> parseFunc,
                                                                              Function<Class<? extends T>, T> defaultValueFunc) {
        return new AbstractFullConverter<T, ConvertT>() {
            @Override public Class<T> getType() {return type;}
            @Override public Class<ConvertT> getConvertType() {return convertType;}
            @Override public ConvertT convert(T value) {
                return convertFunction.apply(value == null ? defaultValueFunc.apply(type) : value);
            }
            @Override public <R extends T> R parse(Class<R> type, ConvertT value) {
                requierCanParseTo(type);
                return (R) (value == null ? defaultValueFunc.apply(type) : parseFunc.apply(type, value));
            }
            @Override public boolean canConvertFrom(Class type_) {return type.isAssignableFrom(toBoxedType(type_));}
        };
    }

    /**
     * Same as {@link Converters#fullConverterInherit} above, but with default value null.
     */
    public static <T,ConvertT> FullConverter<T,ConvertT> fullConverterInherit(Class<T> type,
                                                                              Class<ConvertT> convertType,
                                                                              Function<T, ConvertT> convertFunction,
                                                                              BiFunction<Class<? extends T>, ConvertT, T> parseFunc) {
        return fullConverterInherit(type, convertType, convertFunction, parseFunc, (c) -> null);
    }



}
