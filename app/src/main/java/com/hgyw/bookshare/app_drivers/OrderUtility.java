package com.hgyw.bookshare.app_drivers;

import android.os.AsyncTask;
import android.os.Looper;

import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.concurrent.ExecutionException;

/**
 * Because the order should not refer to book supplier, bur refer to supplier and book directly,
 * use these methods to get the supplierId of order.
 */
public final class OrderUtility {

    private OrderUtility() {}

    // field for caching bookSupplier
    private static BookSupplier bs = new BookSupplier();

    public static BookSupplier getBsOfOrder(Order order) {
        if (bs.getId() == order.getBookSupplierId()) return bs;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            try {
                return new AsyncTask<Void, Void, BookSupplier>() {
                    @Override
                    protected BookSupplier doInBackground(Void... params) {
                        return getBsOfOrder(order);
                    }
                }.execute().get();
            } catch (InterruptedException | ExecutionException ignored) {}
        }
        GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
        bs = access.retrieve(BookSupplier.class, order.getBookSupplierId());
        return bs;
    }

    /**
     * Because the order should not refer to book supplier, bur refer to supplier and book directly,
     * use this method to get the supplierId of order.
     * @param order
     * @return
     */
    public synchronized static long getSupplierId(Order order) {
        return getBsOfOrder(order).getSupplierId();
    }

    /**
     * Because the order should not refer to book supplier, bur refer to supplier and book directly,
     * use this method to get the bookId of order.
     * @param order
     * @return
     */
    public synchronized static long getBookId(Order order) {
        return getBsOfOrder(order).getBookId();
    }


}
