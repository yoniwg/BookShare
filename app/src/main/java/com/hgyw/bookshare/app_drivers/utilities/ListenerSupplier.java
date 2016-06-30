package com.hgyw.bookshare.app_drivers.utilities;

import com.annimon.stream.Stream;

import java.util.Objects;

/**
 * <p>Interface with method that gets listener class and try to return listener instance of the
 * listener class.
 * <P>The purpose of this listener is allowing activities to find a listener in all its fragments.</P>
 */
public interface ListenerSupplier {
    /**
     * @param listenerClass The class object of listener
     * @param <T> The type of listener
     * @return an instance of listenerClass, or null if not found.
     */

    <T> T tryGetListener(Class<T> listenerClass);

}
