package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.DateRangeBar;
import com.hgyw.bookshare.app_drivers.GoodAsyncListAdapter;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.OrderStatus;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.SupplierAccess;

import java.util.Date;
import java.util.List;

/**
 * Created by haim7 on 19/05/2016.
 */
public class SupplierOrdersFragment extends ListFragment implements TitleFragment, DateRangeBar.DateRangeListener {

    private SupplierAccess sAccess;

    private GoodAsyncListAdapter<Order> adapter;

    private Activity activity;
    private Date[] dateRange;

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
        registerForContextMenu(getListView());

        setEmptyText(getString(R.string.no_items_list_view));

        DateRangeBar dateRangeBar = (DateRangeBar) view.findViewById(R.id.dateRangeBar);
        dateRangeBar.setDateRangeListener(this);
        onRangeChange(dateRangeBar); // initial values (the 'dateRange' field)


        adapter = new GoodAsyncListAdapter<Order>(activity, R.layout.old_order_list_item, this) {
            @Override
            public List<Order> retrieveList() {
                return sAccess.retrieveOrders(dateRange[0], dateRange[1], false);
            }

            @Override
            public Object[] retrieveData(Order order) {
                    BookSupplier bookSupplier = sAccess.retrieve(BookSupplier.class, order.getBookSupplierId());
                    Book book = sAccess.retrieve(Book.class, bookSupplier.getBookId());
                    Transaction transaction = sAccess.retrieve(Transaction.class, order.getTransactionId());
                    User customer = sAccess.retrieve(User.class, transaction.getCustomerId());
                    ImageEntity bookImage = sAccess.retrieveOptional(ImageEntity.class,book.getImageId()).orElse(null);
                    return new Object[] {bookSupplier, book, transaction, customer, bookImage};
            }

            @Override
            public void applyDataOnView(Order order, Object[] data, View view) {
                    ObjectToViewAppliers.apply(view, order);
                    ObjectToViewAppliers.apply(view, (BookSupplier) data[0]);
                    ObjectToViewAppliers.apply(view, (Book) data[1]);
                    ObjectToViewAppliers.apply(view, (Transaction) data[2]);
                    ObjectToViewAppliers.apply(view, (User) data[3]);
                    ObjectToViewAppliers.apply(view, (ImageEntity) data[4]);
            }
        };
    }

    @Override
    public void onRangeChange(DateRangeBar dateRangeBar) {
        dateRange = new Date[]{dateRangeBar.getDateFrom(), dateRangeBar.getDateTo()};
        if (adapter != null) adapter.refreshRetrieveList();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Order order = adapter.getItem(info.position);
        if (order.getOrderStatus() == OrderStatus.WAITING_FOR_CANCEL) {
            MenuItem itemCancel = menu.add(R.string.cancel_confirm);
            MenuItem itemReject = menu.add(R.string.cancel_reject);
            setMenuItemListener(v, itemCancel);
            setMenuItemListener(v, itemReject);
        }
        if (order.getOrderStatus() == OrderStatus.NEW_ORDER) {
            MenuItem itemConfirm = menu.add(R.string.confirm_order);
            setMenuItemListener(v, itemConfirm);
        }
        if (order.getOrderStatus() == OrderStatus.WAITING_FOR_PAYING) {
            MenuItem itemConfirm = menu.add(R.string.confirm_payment);
            setMenuItemListener(v, itemConfirm);
        }
    }

    private void setMenuItemListener(final View v, MenuItem menuItem) {
        int messageId;
        OrderStatus orderStatus;
        int toastMessageId;
        if (menuItem.getTitle() == v.getContext().getString(R.string.cancel_confirm)){
            messageId = R.string.cancel_order_massage;
            orderStatus = OrderStatus.CANCELED;
            toastMessageId = R.string.toast_order_canceled;
        }else if (menuItem.getTitle() == v.getContext().getString(R.string.cancel_reject)){
            messageId = R.string.reject_cancel_oreder_message;
            orderStatus = OrderStatus.WAITING_FOR_PAYING;
            toastMessageId = R.string.toast_order_cancel_reject;
        }else if (menuItem.getTitle() == v.getContext().getString(R.string.confirm_order)){
            messageId = R.string.confirm_order_message;
            orderStatus = OrderStatus.WAITING_FOR_PAYING;
            toastMessageId = R.string.toast_order_waiting_for_pay;
        }else { //if (menuItem.getTitle() == v.getContext().getString(R.string.confirm_payment)){
            messageId = R.string.confirm_payment_message;
            orderStatus = OrderStatus.SENT;
            toastMessageId = R.string.toast_order_sent;
        }
        menuItem.setOnMenuItemClickListener(item -> {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Order order = adapter.getItem(info.position);

            //show yes/no alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(messageId)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                sAccess.updateOrderStatus(order, orderStatus);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                adapter.update(order);
                                Toast.makeText(v.getContext(), toastMessageId, Toast.LENGTH_SHORT).show();
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        long orderId = ((Order) l.getAdapter().getItem(position)).getId();
        IdReference orderRef = IdReference.of(Order.class, orderId);
        startActivity(IntentsFactory.newEntityIntent(activity, orderRef));
    }

    @Override
    public @StringRes int getFragmentTitle() {
        return R.string.supplier_orders_title;
    }

    @Override
    public void onDestroy() {
        if (adapter != null) adapter.cancel();
        super.onDestroy();
    }

}
