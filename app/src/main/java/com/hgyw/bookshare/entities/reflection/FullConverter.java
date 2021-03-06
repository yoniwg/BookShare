package com.hgyw.bookshare.entities.reflection;

/**
 * An interface for converter, an object that convert type to other type (the 'convert-type'), and
 * parse the type from the convert-type to the source type. <br>
 * that it's a name that describe the type, for example 'BIGINT' for long for some databases. <br>
 * ('Full converter' because it can both convert and parse).
 */
public interface FullConverter<T,ConvertT> {


    /**
     * @return The class object of source type
     */
    Class<T> getType();

    /**
     * @return The class object of convert type
     */
    Class<ConvertT> getConvertType();

    /**
     * CHeck whether this converter can convert from type.
     */
    boolean canConvertFrom(Class<?> type);

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
     * @return string of sql-type ("BIGINT" "TEXT" "DATE" etc.)
     */
    String getSqlType();

    /**
     * @return same object with new sqlTypeName. can be this object depend on implementation.
     */
    FullConverter<T,ConvertT> withSqlType(String sqlTypeName);

    /**
     * Returns a converter for same convert-type as this converter, but the source-type R is differ,
     * by convert from new source-type R to this source-type T and parse from it.
     */
    <T2> FullConverter<T2,ConvertT> subConverter(FullConverter<T2,T> subFullConverter);

}
