package com.hgyw.bookshare.app_fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.CustomerAccess;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A fragment representing the cart.
 * <p>
 */
public class OldOrdersFragment extends AbstractFragment<CustomerAccess> {


    ApplyObjectAdapter<Order> adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OldOrdersFragment() {
        super(R.layout.fragment_standard_list, R.menu.menu_old_orders, R.string.old_orders_fragment_title);
    }

    public static OldOrdersFragment newInstance() {
        OldOrdersFragment fragment = new OldOrdersFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListView listView = (ListView) getActivity().findViewById(R.id.mainListView);

        Date yearBefore = new Date();
        yearBefore.setYear(yearBefore.getYear() - 1);
        List<Order> ordersList = new ArrayList<>(access.retrieveOrders(yearBefore, new Date()));

        adapter = new ApplyObjectAdapter<Order>(getActivity(), R.layout.old_order_list_item, ordersList) {
            @Override
            protected void applyOnView(View view, int position) {
                Order order = getItem(position);
                ObjectToViewAppliers.apply(view, order);
                BookSupplier bookSupplier = access.retrieve(BookSupplier.class, order.getBookSupplierId());
                ObjectToViewAppliers.apply(view, bookSupplier);
                Book book = access.retrieve(Book.class, bookSupplier.getBookId());
                ObjectToViewAppliers.apply(view, book);
                User supplier = access.retrieve(User.class, bookSupplier.getSupplierId());
                ObjectToViewAppliers.apply(view, supplier);
                Transaction transaction= access.retrieve(Transaction.class, order.getTransactionId());
                ObjectToViewAppliers.apply(view, transaction);

            }
        };
        listView.setAdapter(adapter);

    }

}
