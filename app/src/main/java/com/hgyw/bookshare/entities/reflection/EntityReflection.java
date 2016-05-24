package com.hgyw.bookshare.entities.reflection;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.User;

/**
 * Created by haim7 on 04/05/2016.
 */
public class EntityReflection {
    private EntityReflection() {}

    /**
     * Returns predicate that check whether an item of class T refer to all referredItems
     * @param referringClass
     * @param referredItems
     * @param <T>
     * @return
     */
    public static <T extends Entity> Predicate<T> predicateEntityReferTo(Class<T> referringClass, IdReference ... referredItems) {
        Map<Class<? extends Entity>, Property> properties = getReferringProperties(referringClass);
        return item -> Stream.of(referredItems).allMatch(refItem -> {
            Property refTypeProperty = properties.get(refItem.getEntityType());
            return refTypeProperty != null && ((long) refTypeProperty.get(item)) == refItem.getId();
        });
    }

    /**
     *
     * @param referringClass
     * @return
     */
    public static Map<Class<? extends Entity>, Property> getReferringProperties(Class<? extends Entity> referringClass) {
        return Stream.of(PropertiesReflection.getPropertiesMap(referringClass).values())
                .filter(p -> p.getFieldAnnotation(EntityReference.class) != null)
                .collect(Collectors.toMap(p -> p.getFieldAnnotation(EntityReference.class).value(), p -> p));
    }

    public static List<Class<? extends Entity>> getEntityTypes() {
        List<Class<? extends Entity>> list = new ArrayList<>();
        list.add(Book.class);
        list.add(BookSupplier.class);
        list.add(User.class);
        // TODO
        return list;
    }
}
