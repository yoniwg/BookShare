package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Stream;

import java.util.Collection;
import java.util.List;

/**
 * Created by haim7 on 26/05/2016.
 */
public interface PropertiesConvertManager {

    Stream<Property> streamFlatProperties(Class aClass);
    Collection<Converter> getConverters();
    void setConverters(Collection<Converter> converter);
    Converter findConverter(Class aClass);
    boolean isConverterExists(Class aClass);

}

