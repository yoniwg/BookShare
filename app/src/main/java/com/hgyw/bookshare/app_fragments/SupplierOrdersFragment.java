package com.hgyw.bookshare.app_fragments;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.SupplierAccess;

import java.util.Date;
import java.util.List;

/**
 * Created by haim7 on 19/05/2016.
 */
public class SupplierOrdersFragment extends AbstractFragment<SupplierAccess> {
    public SupplierOrdersFragment() {
        super(R.layout.fragment_standard_list, 0, R.string.supplier_orders_title);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupplierAccess sAccess = (SupplierAccess) access;
        ListView listView = (ListView) view.findViewById(R.id.mainListView);
        List<Order> orders = sAccess.retrieveOrders(new Date(0), new Date());
        listView.setAdapter(new ApplyObjectAdapter<Order>(getActivity(), R.layout.supplier_order_list_item, orders) {
            @Override
            protected void applyOnView(View view, int position) {
                Order order = getItem(position);
                ObjectToViewAppliers.apply(view, order);
                Book book = sAccess.retrieve(Book.class, order.getBookSupplierId());
                ObjectToViewAppliers.apply(view, book);
                Transaction transaction = sAccess.retrieve(Transaction.class, order.getTransactionId());
                ObjectToViewAppliers.apply(view, transaction);
                User user = sAccess.retrieve(User.class, order.getTransactionId());
                ObjectToViewAppliers.apply(view, user);
            }
        });
    }
}
