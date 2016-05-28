package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Entity;

/**
 * Crud interface,  with method {@link StreamCrud#streamAll(Class)}, to stream whole
 * database table.
 */
 interface StreamCrud extends Crud {

    /**
     * Stream whole items in database of entity {@code entityType}.
     * @param entityType the class of entity
     * @param <T> the type of entity
     * @return a stream contains the items.
     */
    <T extends Entity> Stream<T> streamAll(Class<T> entityType);

}
