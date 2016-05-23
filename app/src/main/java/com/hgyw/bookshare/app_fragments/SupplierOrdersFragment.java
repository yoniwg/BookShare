package com.hgyw.bookshare.app_fragments;

import android.app.ListFragment;
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
import com.hgyw.bookshare.app_drivers.DateRangeBar;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.SupplierAccess;

import java.util.Date;
import java.util.List;

/**
 * Created by haim7 on 19/05/2016.
 */
public class SupplierOrdersFragment extends ListFragment implements TitleFragment {

    private SupplierAccess sAccess;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders_supplier, container, false);
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
        List<Order> orders = sAccess.retrieveOrders(dateRangeBar.getDateFrom(), dateRangeBar.getDateTo(), false);
        setListAdapter(new ApplyObjectAdapter<Order>(getActivity(), R.layout.old_order_list_item, orders) {
            @Override
            protected void applyOnView(View view, int position) {
                Order order = getItem(position);
                ObjectToViewAppliers.apply(view, order);
                BookSupplier bookSupplier = sAccess.retrieve(BookSupplier.class, order.getBookSupplierId());
                ObjectToViewAppliers.apply(view, bookSupplier);
                Book book = sAccess.retrieve(Book.class, bookSupplier.getBookId());
                ObjectToViewAppliers.apply(view, book);
                Transaction transaction = sAccess.retrieve(Transaction.class, order.getTransactionId());
                ObjectToViewAppliers.apply(view, transaction);
                User customer = sAccess.retrieve(User.class, transaction.getCustomerId());
                ObjectToViewAppliers.apply(view, customer);
            }
        });

    }

    @Override
    public @StringRes int getFragmentTitle() {
        return R.string.supplier_orders_title;
    }
}
