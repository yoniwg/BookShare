package com.hgyw.bookshare.app_fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.CancelableLoadingDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.app_drivers.ObjectToViewUpdates;
import com.hgyw.bookshare.app_drivers.OrderUtility;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.OrderStatus;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;
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
        new CancelableLoadingDialogAsyncTask<Object[]>(getActivity()) {

            @Override
            protected Object[] retrieveDataAsync() {
                GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
                Order order = access.retrieve(Order.class, entityId);
                Book book = access.retrieve(Book.class, OrderUtility.getBookId(order));
                Transaction transaction = access.retrieve(Transaction.class, order.getTransactionId());
                ImageEntity bookImage = access.retrieveOptional(ImageEntity.class, book.getImageId()).orElse(null);
                User user = access.retrieve(User.class, access.getUserType() == UserType.CUSTOMER ? OrderUtility.getSupplierId(order) : transaction.getCustomerId());
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
                View changeStatusButton = view.findViewById(R.id.changeStatusButton);
                if (changeStatusButton != null) updateChangeStatusButton((Button) changeStatusButton, view, order);
            }

            @Override
            protected void onCancel() {
                getActivity().finish();
            }

        }.execute();

    }

    private void updateChangeStatusButton(Button changeStatusButton, View viewOfStatus, Order order) {
        // TODO unify with OrdersFragment.onCreateContextMenu()
        OrderStatus nextOrderStatus;
        int buttonLabelId;
        int messageId;
        switch (order.getOrderStatus()) {
            case NEW_ORDER:
                nextOrderStatus = OrderStatus.CANCELED;
                buttonLabelId = R.string.cancel_order;
                messageId = R.string.cancel_order_massage;
                break;
            case SENT:
                nextOrderStatus = OrderStatus.CLOSED;
                buttonLabelId = R.string.confirm_receive;
                messageId = R.string.confirm_order_message;
                break;
            default:
                changeStatusButton.setVisibility(View.GONE);
                return;
        }
        CustomerAccess cAccess = AccessManagerFactory.getInstance().getCustomerAccess();
        Context context = getActivity();
        changeStatusButton.setText(buttonLabelId);
        changeStatusButton.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setMessage(messageId)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            cAccess.updateOrderStatus(order, nextOrderStatus);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            ObjectToViewAppliers.apply(viewOfStatus, order);
                            updateChangeStatusButton(changeStatusButton, viewOfStatus, order);
                            Toast.makeText(v.getContext(), R.string.order_status_changed, Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                }).setNeutralButton(R.string.no, null)
                .create().show()
        );
    }

}
