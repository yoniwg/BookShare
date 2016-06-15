package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.CancelableLoadingDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;

import java.util.List;

/**
 * Created by haim7 on 23/05/2016.
 */
public class TransactionFragment extends EntityFragment {

    CustomerAccess cAccess = AccessManagerFactory.getInstance().getCustomerAccess();

    private Activity activity;
    private Transaction transaction;

    @Override public void onAttach(Context context) {super.onAttach(context);activity = (Activity) context;}
    @Override public void onAttach(Activity activity) {super.onAttach(activity);this.activity = activity;}

    public TransactionFragment() {
        super(R.layout.fragment_transaction, 0, R.string.transaction_details);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup linearLayout = (ViewGroup) view.findViewById(R.id.mainListView);

        new CancelableLoadingDialogAsyncTask<Void, Void, Void>(activity) {
            List<Order> orders;
            @Override
            protected Void retrieveDataAsync(Void... params) {
                transaction = cAccess.retrieve(Transaction.class, entityId);
                orders = cAccess.retrieveOrdersOfTransaction(transaction);
                return null;
            }

            @Override
            protected void doByData(Void aVoid) {
                Utility.addViewsByList(linearLayout, orders, R.layout.order_for_transaction_list_item, TransactionFragment.this::updateOrder);
            }

            @Override
            protected void onCancel() {
                activity.finish();
            }
        }.execute();


    }

    private void updateOrder(View view, Order order) {
        new AsyncTask<Void, Void, Object[]>() {
            @Override
            protected Object[] doInBackground(Void... params) {
                BookSupplier bookSupplier = cAccess.retrieve(BookSupplier.class, order.getBookSupplierId());
                Book book = cAccess.retrieve(Book.class, bookSupplier.getBookId());
                User supplier = cAccess.retrieve(User.class, bookSupplier.getSupplierId());
                return new Object[] {bookSupplier, book, supplier};
            }

            @Override
            protected void onPostExecute(Object[] data) {
                ObjectToViewAppliers.apply(view, transaction);
                ObjectToViewAppliers.apply(view, order);
                ObjectToViewAppliers.apply(view, (BookSupplier) data[0]);
                ObjectToViewAppliers.apply(view, (Book) data[1]);
                ObjectToViewAppliers.apply(view, (User) data[2]);
            }
        }.execute();
    }
}
