package com.hgyw.bookshare.app_drivers.utilities;

/**
 * Created by haim7 on 22/05/2016.
 */
public class ListenerSupplierHelper {
    private ListenerSupplierHelper(){}

    /**
     * Try cast activity to T, or to ListenerSupplier and call tryGetListener(T.class).
     * Returns not-null instance of T, or throw ClassCastException.
     * Don't try call it before the activity has been fully instantiated. on fragment call it in
     * {@code onActivityCreate()} or something like that.
     * @throws NullPointerException if activity == null
     */
    public static <T> T getListenerFromActivity(Class<T> listenerClass, Object activity) {
        String internalExceptionMessage = "";
        if (listenerClass.isInstance(activity)) {
            return listenerClass.cast(activity);
        }
        if (activity instanceof ListenerSupplier) {
            T listener = ((ListenerSupplier) activity).tryGetListener(listenerClass);
            if (listener != null) return listener;
        }
        String message = "The object " + activity + " should provide instance of " + listenerClass.getSimpleName();
        throw new ClassCastException(message + internalExceptionMessage);
    }

    /**
     * Returns the first object (from objects) is instance of T, or null if no such object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T tryGetListenerFromObjects(Class<T> listenerClass, Object... objects) {
        for (Object object : objects) {
            if (listenerClass.isInstance(object)) return (T) object;
        }
        return null;
    }

    /*
    @SuppressWarnings("unchecked")
    public static <T> T tryGetListenerFromObjects(Class<T> listenerClass, Object object) {
        if (listenerClass.isInstance(object)) return (T) object;
        return null;
    }*/


}
