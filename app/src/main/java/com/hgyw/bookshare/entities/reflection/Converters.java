package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by haim7 on 25/05/2016.
 */
public class Converters {

    private static abstract class AbstractConverter<T,ConvertT> implements Converter<T,ConvertT> {
        public <R> Converter<R,ConvertT> subConverter(Class<R> subType, Function<R,T> subConvert, Function<T,R> subParse) {
            return new AbstractConverter<R, ConvertT>() {
                public Class<R> getType() {return subType;}
                public Class<ConvertT> getConvertType() { return AbstractConverter.this.getConvertType(); }
                public String getConvertTypeName() { return AbstractConverter.this.getConvertTypeName(); }
                public ConvertT convert(R value) { return AbstractConverter.this.convert(subConvert.apply(value)); }
                public R parse(ConvertT value) { return subParse.apply(AbstractConverter.this.parse(value)); }
            };
        }
    }

    public static <T> Converter<T,T> ofIdentity(Class<T> type, String convertTypeName) {
        return new AbstractConverter<T, T>()  {
            public Class<T> getType() {return type;}
            public Class<T> getConvertType() {return type;}
            public String getConvertTypeName() { return convertTypeName; }
            public T convert(T value) {return value;}
            public T parse(T value) {return value;}
        };
    }

    public static <T,ConvertT> Converter<T,ConvertT> simple(Class<T> type,
                                                            Class<ConvertT> convertType,
                                                            Function<T, ConvertT> convertFunction,
                                                            Function<ConvertT, T> parseFunc,
                                                            String convertTypeName) {
        return new AbstractConverter<T, ConvertT>() {
            public Class<T> getType() {return type;}
            public Class<ConvertT> getConvertType() {return convertType;}
            public String getConvertTypeName() {return convertTypeName;}
            public ConvertT convert(T value) {return value == null ? null : convertFunction.apply(value);}
            public T parse(ConvertT value) {return value == null ? null : parseFunc.apply(value);}
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

    public static <T extends Enum<T>> Converter<T,Integer> enumToIntegerConverter(Class<T> type, String convertTypeName) {
        return Converters.simple(type, Integer.class, Enum::ordinal, i -> type.getEnumConstants()[i], convertTypeName);
    }


}
