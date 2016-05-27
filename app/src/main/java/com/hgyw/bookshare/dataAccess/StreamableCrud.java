package com.hgyw.bookshare.dataAccess;

import com.annimon.stream.Stream;
import com.hgyw.bookshare.entities.Entity;

/**
 * Created by haim7 on 27/05/2016.
 */
 interface StreamableCrud extends Crud {

    <T extends Entity> Stream<T> streamAll(Class<T> entityType);

}
