package com.hgyw.bookshare.entities.reflection;

/**
 * An interface for converter, an object that convert type to other type (the 'convert-type'), and
 * parse the type from the convert-type to the source type. <br/>
 * that it's a name that describe the type, for example 'BIGINT' for long for some databases.
 */
public interface OneSideConverter<T,ConvertT> {

    /**
     * @return The class object of source type
     */
    Class<T> getType();

    /**
     * Convert from value of T to ConvertT.
     * @return converted object, or null if value is null.
     */
    ConvertT convert(T value);

    boolean canConvertFrom(Class<?> type);

    String getSqlTypeName();


}
