package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.function.Function;

/**
 * Created by haim7 on 26/05/2016.
 */
public interface Converter<T,ConvertT> {
    Class<T> getType();
    Class<ConvertT> getConvertType();
    String getConvertTypeName();
    ConvertT convert(T value);
    T parse(ConvertT value);
    <R> Converter<R,ConvertT> subConverter(Class<R> subType, Function<R,T> subConvert, Function<T,R> subParse);
}
