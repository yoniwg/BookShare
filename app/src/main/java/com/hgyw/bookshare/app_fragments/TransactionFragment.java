package com.hgyw.bookshare.app_fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.hgyw.bookshare.R;
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

    public TransactionFragment() {
        super(R.layout.fragment_transaction, 0, R.string.transaction_details);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Transaction transaction = cAccess.retrieve(Transaction.class, entityId);
        List<Order> orders = cAccess.retrieveOrdersOfTransaction(transaction);
        ViewGroup linearLayout = (ViewGroup) view.findViewById(R.id.mainListView);
        Utility.addViewsByList(linearLayout, orders, getActivity().getLayoutInflater(), R.layout.old_order_list_item, this::updateOrder);
    }

    private void updateOrder(View view, Order order) {
        ObjectToViewAppliers.apply(view, order);
        BookSupplier bookSupplier = cAccess.retrieve(BookSupplier.class, order.getBookSupplierId());
        Book book = cAccess.retrieve(Book.class, bookSupplier.getBookId());
        User supplier = cAccess.retrieve(User.class, bookSupplier.getSupplierId());
        ObjectToViewAppliers.apply(view, book);
        ObjectToViewAppliers.apply(view, supplier);
    }
}
