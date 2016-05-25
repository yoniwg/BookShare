package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.function.Function;

/**
 * Created by haim7 on 25/05/2016.
 */
public class Converters {
    public interface Converter<T,ConvertT> {
        Class<T> getType();
        Class<ConvertT> getConvertType();
        String getConvertTypeName();
        ConvertT convert(T value);
        T parse(ConvertT value);
    }

    public static <T> Converter<T,T> ofIdentity(Class<T> type, String convertTypeName) {
        return new Converter<T, T>() {
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
        return new Converter<T, ConvertT>() {
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


}
