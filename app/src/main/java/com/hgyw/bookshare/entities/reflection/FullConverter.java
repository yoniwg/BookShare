package com.hgyw.bookshare.entities.reflection;

/**
 * An interface for converter, an object that convert type to other type (the 'convert-type'), and
 * parse the type from the convert-type to the source type. <br/>
 * that it's a name that describe the type, for example 'BIGINT' for long for some databases.
 */
public interface FullConverter<T,ConvertT>  extends  OneSideConverter<T,ConvertT>, Parser<T,ConvertT> {

    /**
     * Return a converter for same convert-type as this converter, but the source-type R is differ,
     * by convert from new source-type R to this source-type T and parse from it.
     */
    <T2> FullConverter<T2,ConvertT> subConverter(FullConverter<T2,T> subFullConverter);



}
