package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by haim7 on 27/05/2016.
 */
public class Propertiescatch {
    private final Map<Class, Property[]> propertiesMap = new HashMap<>();
    private final Function<Class, Property[]> propertiesGenerator;
    

    public Propertiescatch(Function<Class, Property[]> propertiesGenerator) {
        this.propertiesGenerator = propertiesGenerator;
    }
    
    public Stream<Property> streamFlatProperties(Class aClass) {
        Property[] props = propertiesMap.get(aClass);
        if (props == null) {
            props = propertiesGenerator.apply(aClass);
            propertiesMap.put(aClass, props);
        }
        return Stream.of(props);
    }

}
