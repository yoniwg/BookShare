package com.hgyw.bookshare.entities.reflection;

/**
 * Created by haim7 on 09/06/2016.
 */
public interface Parser<T,ConvertT> {

    /**
     * @return The class object of convert type
     */
    Class<ConvertT> getConvertType();

    /*
     * Parsed from value of ConvertT to R object.
     * @param <R> Type to parse to.
     * @return parsed object, or null if value is null.
     */
    <R extends T> R parse(Class<R> type, ConvertT value);


    boolean canConvertFrom(Class<?> type);

}
