package com.hgyw.bookshare.app_drivers.utilities;

import com.annimon.stream.Stream;

import java.util.Objects;

/**
 * Created by haim7 on 22/05/2016.
 */
public interface ListenerSupplier {
    /**
     * returns null if not found
     */
    <T> T tryGetListener(Class<T> listenerClass);

}
