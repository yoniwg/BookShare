package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.function.BiFunction;
import com.annimon.stream.function.Function;

import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

/**
 * Created by haim7 on 25/05/2016.
 */
public class Converters {

    private static abstract class AbstractConverter<T,ConvertT> implements Converter<T,ConvertT> {
        @Override public String getSqlTypeName() {return "";}
        protected void requierCanParseTo(Class type) {
            if (!canConvertFrom(type)) throw new IllegalArgumentException("This converter cannot cast to " + type);
        }
        @Override
        public <T2> Converter<T2,ConvertT> subConverter(Converter<T2,T> subConverter) {
            return new AbstractConverter<T2, ConvertT>() {
                @Override public Class<T2> getType() {return subConverter.getType();}
                @Override public boolean canConvertFrom(Class type) {return toBoxedType(type) == getType();}
                @Override public Class<ConvertT> getConvertType() { return AbstractConverter.this.getConvertType(); }
                @Override public ConvertT convert(T2 value) { return AbstractConverter.this.convert(subConverter.convert(value)); }
                @Override public <R extends T2> R parse(Class<R> type, ConvertT value) {
                    return subConverter.parse(type, AbstractConverter.this.parse(subConverter.getConvertType(), value));
                }
            };
        }
    }

    public static <T> Converter<T,T> ofIdentity(Class<T> type) {
        return new AbstractConverter<T, T>()  {
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

    public static <T,ConvertT> Converter<T,ConvertT> simple(Class<T> type,
                                                            Class<ConvertT> convertType,
                                                            Function<T, ConvertT> convertFunction,
                                                            Function<ConvertT, T> parseFunc) {
        return new AbstractConverter<T, ConvertT>() {
            @Override public Class<T> getType() {return type;}
            @Override public boolean canConvertFrom(Class type) {return toBoxedType(type) == getType();}
            @Override public Class<ConvertT> getConvertType() {return convertType;}
            @Override public ConvertT convert(T value) {return value == null ? null : convertFunction.apply(value);}
            @Override public <R extends T> R parse(Class<R> type, ConvertT value) {
                requierCanParseTo(type);
                return value == null ? null : (R) parseFunc.apply(value);
            }
        };
    }

    public static <T,ConvertT> Converter<T,ConvertT> inherit(Class<T> type,
                                                            Class<ConvertT> convertType,
                                                            Function<T, ConvertT> convertFunction,
                                                            BiFunction<Class<? extends T>, ConvertT, T> parseFunc) {
        return new AbstractConverter<T, ConvertT>() {
            @Override public Class<T> getType() {return type;}
            @Override public Class<ConvertT> getConvertType() {return convertType;}
            @Override public ConvertT convert(T value) {return value == null ? null : convertFunction.apply(value);}
            @Override public <R extends T> R parse(Class<R> type, ConvertT value) {
                requierCanParseTo(type);
                return value == null ? null : (R) parseFunc.apply(type, value);
            }
            @Override public boolean canConvertFrom(Class type_) {return type.isAssignableFrom(toBoxedType(type_));}
        };
    }

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

    public static <T> T tryNewInstanceOrThrow(Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("The class should produce a public empty constructor.", e);
        }
    }


}
