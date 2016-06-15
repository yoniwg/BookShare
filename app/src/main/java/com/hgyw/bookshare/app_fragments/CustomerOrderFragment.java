package com.hgyw.bookshare.app_fragments;

import android.os.Bundle;
import android.view.View;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.CancelableLoadingDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;

/**
 * Created by haim7 on 15/06/2016.
 */
public class CustomerOrderFragment extends EntityFragment {
    private Order order;

    public CustomerOrderFragment() {
        super(R.layout.fragment_order_customer, 0, R.string.order_details);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            new CancelableLoadingDialogAsyncTask<Void, Void, Object[]>(getActivity()) {
                @Override
                protected Object[] retrieveDataAsync(Void... params) {
                    CustomerAccess cAccess = AccessManagerFactory.getInstance().getCustomerAccess();
                    order = cAccess.retrieve(Order.class, entityId);

                    BookSupplier bookSupplier = cAccess.retrieve(BookSupplier.class, order.getBookSupplierId());
                    Book book = cAccess.retrieve(Book.class, bookSupplier.getBookId());
                    User supplier = cAccess.retrieve(User.class, bookSupplier.getSupplierId());
                    Transaction transaction = cAccess.retrieve(Transaction.class, order.getTransactionId());
                    ImageEntity bookImage = (book.getImageId() == 0) ?
                            null : cAccess.retrieve(ImageEntity.class, book.getImageId());
                    return new Object[]{bookSupplier, book, supplier, transaction, bookImage, order};
                }

                @Override
                protected void doByData(Object[] data) {
                    ObjectToViewAppliers.apply(view, (Order) data[5]);
                    ObjectToViewAppliers.apply(view, (BookSupplier) data[0]);
                    ObjectToViewAppliers.apply(view, (Book) data[1], false);
                    ObjectToViewAppliers.apply(view, (User) data[2]);
                    ObjectToViewAppliers.apply(view, (Transaction) data[3]);
                    ObjectToViewAppliers.apply(view, (ImageEntity) data[4]);
                }

                @Override
                protected void onCancel() {
                    getActivity().finish();
                }

            }.execute();
        }

        // set listeners
        Utility.setListenerForAll(view, v -> {
            long bookId = Utility.getBookSupplier(order).getBookId();
            new CancelableLoadingDialogAsyncTask<Void, View, String>(getActivity()) {
                @Override
                protected String retrieveDataAsync(Void... params) {
                    Book book = access.retrieve(Book.class, bookId);
                    return book.getAuthor();
                }

                @Override
                protected void doByData(String query) {
                    Utility.startSearchActivity(getActivity(), query);
                }

                @Override
                protected void onCancel() {}
            }.execute();
        }, R.id.bookAuthor, R.id.bookAuthorIcon);

        Utility.setListenerForAll(view, v -> {
            IdReference supplier = IdReference.of(User.class, Utility.getBookSupplier(order).getSupplierId());
            startActivity(IntentsFactory.newEntityIntent(getActivity(), supplier));
        }, R.id.userFirstName, R.id.userLastName, R.id.userFullName, R.id.orderUnitPriceIcon);

    }


}
