package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;

import java.util.Map;

import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.EntityReference;
import com.hgyw.bookshare.entities.IdReference;

/**
 * Created by haim7 on 04/05/2016.
 */
public class EntityReflection {
    private EntityReflection() {}

    public static <T extends Entity> Predicate<T> predicateEntityReferTo(Class<T> referringClass, IdReference ... referredItems) {
        Map<Class<T>, Property> properties = Stream.of(PropertiesReflection.getPropertiesMap(referringClass).values())
                .filter(p -> p.getFieldAnnotation(EntityReference.class) != null)
                .collect(Collectors.toMap(p -> p.getFieldAnnotation(EntityReference.class).value(), p -> p));
        return item -> Stream.of(referredItems).allMatch(refItem -> {
            Property refTypeProperty = properties.get(refItem.getEntityType());
            return refTypeProperty != null && ((long) refTypeProperty.get(item)) == refItem.getId();
        });

    }

}
