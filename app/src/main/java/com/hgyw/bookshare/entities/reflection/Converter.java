package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.function.Function;

/**
 * An interface for converter, an object that convert type to other type (the 'convert-type'), and
 * parse the type from the convert-type to the source type. <br/>
 * that it's a name that describe the type, for example 'BIGINT' for long for some databases.
 */
public interface Converter<T,ConvertT> {

    /**
     * @return The class object of source type
     */
    Class<T> getType();
    /**
     * @return The class object of convert type
     */
    Class<ConvertT> getConvertType();

    /**
     * Convert from value of T to ConvertT.
     * @return converted object, or null if value is null.
     */
    ConvertT convert(T value);

    /*
     * Parsed from value of ConvertT to R object.
     * @param <R> Type to parse to.
     * @return parsed object, or null if value is null.
     */
    <R extends T> R parse(Class<R> type, ConvertT value);

    /**
     * Return a converter for same convert-type as this converter, but the source-type R is differ,
     * by convert from new source-type R to this source-type T and parse from it.
     */
    <T2> Converter<T2,ConvertT> subConverter(Converter<T2,T> subConverter);


    boolean canConvertFrom(Class<?> type);

    String getSqlTypeName();


}
