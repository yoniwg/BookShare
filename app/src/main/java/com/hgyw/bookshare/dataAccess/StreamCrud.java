package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.IdReference;

/**
 * Crud interface, with method {@link StreamCrud#streamAll(Class)}, to stream whole
 * database table.
 */
 interface StreamCrud {

    /**
     * @see DataAccess#create(Entity)
     */
    void create(Entity item);

    /**
     * @see DataAccess#update(Entity)
     */
    void update(Entity item);

    /**
     * @see DataAccess#delete(IdReference)
     */
    void delete(IdReference item);

    /**
     * @see DataAccess#retrieve(Class, long)
     */
    <T extends Entity> T retrieve(Class<T> entityClass, long id);


    /**
     * Stream whole items in database of entity {@code entityType}.
     * @param entityType the class of entity
     * @param <T> the type of entity
     * @return a stream contains the items.
     */
    <T extends Entity> Stream<T> streamAll(Class<T> entityType);

}
