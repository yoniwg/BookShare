package com.hgyw.bookshare.app_fragments;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.View;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.CancelableLoadingDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.app_drivers.ObjectToViewUpdates;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

/**
 * Created by haim7 on 15/06/2016.
 */
public class OrderFragment extends EntityFragment {

    public OrderFragment() {
        super(layoutByUser(), 0, R.string.order_details);
    }

    public static @LayoutRes int layoutByUser() {
        UserType userType = AccessManagerFactory.getInstance().getCurrentUserType();
        if (userType == UserType.CUSTOMER) return R.layout.fragment_order_customer;
        if (userType == UserType.SUPPLIER) return R.layout.fragment_order_supplier;
        throw new IllegalStateException("The fragment require customer or supplier access.");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new CancelableLoadingDialogAsyncTask<Void, Void, Object[]>(getActivity()) {

            @Override
            protected Object[] retrieveDataAsync(Void... params) {
                GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
                Order order = access.retrieve(Order.class, entityId);
                Book book = access.retrieve(Book.class, Utility.getBookId(order));
                Transaction transaction = access.retrieve(Transaction.class, order.getTransactionId());
                ImageEntity bookImage = access.retrieveOptional(ImageEntity.class, book.getImageId()).orElse(null);
                User user = access.retrieve(User.class, access.getUserType() == UserType.CUSTOMER ? Utility.getSupplierId(order) : transaction.getCustomerId());
                return new Object[]{order, book, user, transaction, bookImage};
            }

            @Override
            protected void doByData(Object[] data) {
                Order order = (Order) data[0];
                ObjectToViewAppliers.apply(view, order);
                ObjectToViewAppliers.apply(view, (Book) data[1]);
                ObjectToViewAppliers.apply(view, (User) data[2]);
                Transaction transaction = (Transaction) data[3];
                ObjectToViewAppliers.apply(view, transaction);
                ObjectToViewAppliers.apply(view, (ImageEntity) data[4]);

                ObjectToViewUpdates.setListenerToOrder(view, order);
                ObjectToViewUpdates.setListenerToUser(view, (User) data[2]);
                ObjectToViewUpdates.setListenerToTransaction(view, transaction);

                View transactionButton = view.findViewById(R.id.transactionButton);
                if (transactionButton != null) {
                    IdReference transactionRef = IdReference.of(Transaction.class, order.getTransactionId());
                    transactionButton.setOnClickListener(v -> startActivity(IntentsFactory.newEntityIntent(getActivity(), transactionRef)));
                }
            }

            @Override
            protected void onCancel() {
                getActivity().finish();
            }

        }.execute();

    }


}
