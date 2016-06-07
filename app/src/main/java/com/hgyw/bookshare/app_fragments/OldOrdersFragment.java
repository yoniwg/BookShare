package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ListApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.dataAccess.DataAccess;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A fragment representing the cart.
 * <p>
 */
public class OldOrdersFragment extends ListFragment implements TitleFragment {


    ApplyObjectAdapter<Order> adapter;

    private Activity activity;
    @Override public void onAttach(Context context) {super.onAttach(context);activity = (Activity) context;}
    @Override public void onAttach(Activity activity) {super.onAttach(activity);this.activity = activity;}

    public static OldOrdersFragment newInstance() {
        OldOrdersFragment fragment = new OldOrdersFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Date yearBefore = new Date();
        yearBefore.setYear(yearBefore.getYear() - 1);

        new AsyncTask<Void, Void, List<Order>>() {
            public CustomerAccess access = AccessManagerFactory.getInstance().getCustomerAccess();

            @Override
            protected List<Order> doInBackground(Void... params) {
                return access.retrieveOrders(yearBefore, new Date());
            }

            @Override
            protected void onPostExecute(List<Order> orders) {
                adapter = new ListApplyObjectAdapter<Order>(activity, R.layout.old_order_list_item, orders) {
                    @Override
                    protected Object[] retrieveDataForView(Order order) {
                        BookSupplier bookSupplier = access.retrieve(BookSupplier.class, order.getBookSupplierId());
                        Book book = access.retrieve(Book.class, bookSupplier.getBookId());
                        User supplier = access.retrieve(User.class, bookSupplier.getSupplierId());
                        Transaction transaction= access.retrieve(Transaction.class, order.getTransactionId());
                        return new Object[]{bookSupplier,book,supplier,transaction};
                    }
                    @Override
                    protected void applyDataOnView(View view, Order order, Object[] data) {
                        ObjectToViewAppliers.apply(view, order);
                        ObjectToViewAppliers.apply(view, (BookSupplier) data[0]);
                        ObjectToViewAppliers.apply(view, (Book) data[1]);
                        ObjectToViewAppliers.apply(view, (User) data[2]);
                        ObjectToViewAppliers.apply(view,(Transaction) data[3]);
                    }
                };
                setListAdapter(adapter);
            }
        }.execute();
        setEmptyText(getString(R.string.no_items_list_view));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_old_orders, menu);
    }

    @Override
    public int getFragmentTitle() {
        return R.string.old_orders_fragment_title;
    }
}
