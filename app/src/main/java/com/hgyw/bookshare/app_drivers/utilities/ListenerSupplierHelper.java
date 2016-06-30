package com.hgyw.bookshare.app_drivers.utilities;

/**
 * Help methods for interface  {@link ListenerSupplier}
 */
public class ListenerSupplierHelper {
    private ListenerSupplierHelper(){}

    /**
     * <p> Try casting activity to T, or to {@link ListenerSupplier} and call
     * {@link ListenerSupplier#tryGetListener(Class))} .</p>
     * <p>Returns not-null instance of T, or throw ClassCastException if listener don't fount.<br>
     *     (in contract to {@code ListenerSupplier.tryGetListener(Class)} that return null if not fount.)</p>
     * <p>Don't try call it before the activity has been fully instantiated. on fragment call it in
     * {@code onActivityCreate()} or something like that.</p>
     * @param listenerClass the class object of listener
     * @param activity an object by which we get the listener
     * @param <T> Type of listener
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
