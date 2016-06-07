package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.DateRangeBar;
import com.hgyw.bookshare.app_drivers.ListApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.SupplierAccess;

import java.util.Date;
import java.util.List;

/**
 * Created by haim7 on 19/05/2016.
 */
public class SupplierOrdersFragment extends ListFragment implements TitleFragment {

    private SupplierAccess sAccess;

    private Activity activity;
    @Override public void onAttach(Context context) {super.onAttach(context);activity = (Activity) context;}
    @Override public void onAttach(Activity activity) {super.onAttach(activity);this.activity = activity;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content_with_date_range, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sAccess = AccessManagerFactory.getInstance().getSupplierAccess();

        DateRangeBar dateRangeBar = (DateRangeBar) view.findViewById(R.id.dateRangeBar);
        dateRangeBar.setDateRangeListener(this::updateListAdapter);

        updateListAdapter(dateRangeBar);
    }

    private void updateListAdapter(DateRangeBar dateRangeBar) {
        new AsyncTask<Date, Void, List<Order>>() {
            @Override
            protected List<Order> doInBackground(Date... dates) {
                return sAccess.retrieveOrders(dates[0], dates[1], false);
            }

            @Override
            protected void onPostExecute(List<Order> orders) {
                setListAdapter(new ListApplyObjectAdapter<Order>(activity, R.layout.old_order_list_item, orders) {
                    @Override
                    protected Object[] retrieveDataForView(Order order) {
                        BookSupplier bookSupplier = sAccess.retrieve(BookSupplier.class, order.getBookSupplierId());
                        Book book = sAccess.retrieve(Book.class, bookSupplier.getBookId());
                        Transaction transaction = sAccess.retrieve(Transaction.class, order.getTransactionId());
                        User customer = sAccess.retrieve(User.class, transaction.getCustomerId());
                        return new Object[] {bookSupplier, book, transaction, customer};
                    }

                    @Override
                    protected void applyDataOnView(View view, Order order, Object[] data) {
                        ObjectToViewAppliers.apply(view, (BookSupplier) data[0]);
                        ObjectToViewAppliers.apply(view, (Book) data[1]);
                        ObjectToViewAppliers.apply(view, (Transaction) data[2]);
                        ObjectToViewAppliers.apply(view, (User) data[3]);
                    }
                });
            }
        }.execute(dateRangeBar.getDateFrom(), dateRangeBar.getDateTo());
        setEmptyText(getString(R.string.no_items_list_view));

    }

    @Override
    public @StringRes int getFragmentTitle() {
        return R.string.supplier_orders_title;
    }
}
