package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ListApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.OrderStatus;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;

import java.util.Date;
import java.util.List;

/**
 * A fragment representing the cart.
 * <p>
 */
public class OldOrdersFragment extends ListFragment implements TitleFragment {


    ApplyObjectAdapter<Order> adapter;
    public CustomerAccess cAccess;

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
        cAccess = AccessManagerFactory.getInstance().getCustomerAccess();
        registerForContextMenu(getListView());
        Date yearBefore = new Date();
        yearBefore.setYear(yearBefore.getYear() - 1);

        new AsyncTask<Void, Void, List<Order>>() {

            @Override
            protected List<Order> doInBackground(Void... params) {
                return cAccess.retrieveOrders(yearBefore, new Date());
            }

            @Override
            protected void onPostExecute(List<Order> orders) {
                adapter = new ListApplyObjectAdapter<Order>(activity, R.layout.old_order_list_item, orders) {
                    @Override
                    protected Object[] retrieveDataForView(Order order) {
                        BookSupplier bookSupplier = cAccess.retrieve(BookSupplier.class, order.getBookSupplierId());
                        Book book = cAccess.retrieve(Book.class, bookSupplier.getBookId());
                        User supplier = cAccess.retrieve(User.class, bookSupplier.getSupplierId());
                        Transaction transaction= cAccess.retrieve(Transaction.class, order.getTransactionId());
                        ImageEntity bookImage = (book.getImageId() == 0) ?
                                null : cAccess.retrieve(ImageEntity.class,book.getImageId());
                        return new Object[]{bookSupplier,book,supplier,transaction,bookImage};
                    }
                    @Override
                    protected void applyDataOnView(View view, Order order, Object[] data) {
                        ObjectToViewAppliers.apply(view, order);
                        ObjectToViewAppliers.apply(view, (BookSupplier) data[0]);
                        ObjectToViewAppliers.apply(view, (Book) data[1], false);
                        ObjectToViewAppliers.apply(view, (User) data[2]);
                        ObjectToViewAppliers.apply(view,(Transaction) data[3]);
                        ObjectToViewAppliers.apply(view, (ImageEntity) data[4]);
                    }
                };
                setListAdapter(adapter);
            }
        }.execute();
        setEmptyText(getString(R.string.no_items_list_view));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Order order = adapter.getItem(info.position);
        if (order.getOrderStatus() == OrderStatus.NEW_ORDER
                || order.getOrderStatus() == OrderStatus.WAITING_FOR_PAYING) {
            MenuItem itemConfirm = menu.add(R.string.cancel_order);
            setMenuItemListener(v, itemConfirm, order);
        }
        if (order.getOrderStatus() == OrderStatus.SENT) {
            MenuItem itemConfirm = menu.add(R.string.confirm_receive);
            setMenuItemListener(v, itemConfirm, order);
        }
    }

    private void setMenuItemListener(final View v, MenuItem menuItem, Order order) {
        int messageId;
        OrderStatus orderStatus;
        int toastMessageId;
        if (menuItem.getTitle() == v.getContext().getString(R.string.cancel_order)){
            messageId = R.string.cancel_order_massage;
            orderStatus = (order.getOrderStatus() == OrderStatus.NEW_ORDER)?
                    OrderStatus.CANCELED : OrderStatus.WAITING_FOR_CANCEL;
            toastMessageId = (order.getOrderStatus() == OrderStatus.NEW_ORDER)?
                    R.string.toast_order_canceled : R.string.toast_order_wait_cancel;
        }else { //if (menuItem.getTitle() == v.getContext().getString(R.string.confirm_receive)){
            messageId = R.string.confirm_receive_message;
            orderStatus = OrderStatus.CLOSED;
            toastMessageId = R.string.toast_order_closed;
        }
        menuItem.setOnMenuItemClickListener(item -> {
            //show yes/no alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(messageId)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                cAccess.updateOrderStatus(order, orderStatus);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                adapter.notifyDataSetChanged();
                                Toast.makeText(v.getContext(), toastMessageId,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }.execute();
                    })

                    .setNeutralButton(R.string.no, (dialog, which) -> {
                    });

            builder.create().show();
            return true;
        });
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
