package com.hgyw.bookshare.app_fragments;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ListApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.Cart;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.logicAccess.CustomerAccess;

import java.util.AbstractMap;
import java.util.List;

/**
 * A fragment representing the cart.
 * <p>
 */
public class CartFragment extends ListFragment implements TitleFragment {

    public static final String IS_MAIN_FRAGMENT = "is_amount_can_modify";

    ApplyObjectAdapter<Order> adapter;
    private Cart cart = AccessManagerFactory.getInstance().getCustomerAccess().getCart();


    public static CartFragment newInstance(){
        return newInstance(true);
    }

    public static CartFragment newInstance(boolean isMainFragment) {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_MAIN_FRAGMENT, isMainFragment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        registerForContextMenu(getListView());
        List<Order> ordersList = cart.retrieveCartContent();

        adapter = new ListApplyObjectAdapter<Order>(getActivity(), R.layout.order_list_item, ordersList) {
            @Override
            protected Object[] retrieveDataForView(Order order) {
                CustomerAccess cAccess = AccessManagerFactory.getInstance().getCustomerAccess();
                BookSupplier bookSupplier = cAccess.retrieve(BookSupplier.class, order.getBookSupplierId());
                Book book = cAccess.retrieve(Book.class, bookSupplier.getBookId());
                User supplier = cAccess.retrieve(User.class, bookSupplier.getSupplierId());
                ImageEntity bookImage = (book.getImageId() == 0) ?
                        null : cAccess.retrieve(ImageEntity.class,book.getImageId());
                return new Object[]{bookSupplier, book, supplier, bookImage};
            }

            @Override
            protected void applyDataOnView(View view, Order order, Object[] data) {
                ObjectToViewAppliers.apply(view, order);
                ObjectToViewAppliers.apply(view, (BookSupplier) data[0]);
                ObjectToViewAppliers.apply(view, (Book) data[1], false);
                ObjectToViewAppliers.apply(view, (User) data[2]);
                ObjectToViewAppliers.apply(view, (ImageEntity) data[3]);
                NumberPicker orderAmountPicker = (NumberPicker) view.findViewById(R.id.orderAmountPicker);
                if (getArguments().getBoolean(IS_MAIN_FRAGMENT)) {
                    orderAmountPicker.setOnValueChangedListener((picker, oldVal, newVal) -> order.setAmount(newVal));
                } else {
                    orderAmountPicker.setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.orderAmountFinal).setVisibility(View.VISIBLE);
                }
            }
        };
        setListAdapter(adapter);
        setEmptyText(getString(R.string.no_items_list_view));

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(R.string.delete);

        menu.getItem(0).setOnMenuItemClickListener(item -> {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            Order order = (Order) adapter.getItem(info.position);

            //show yes/no alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.delete_order_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        cart.remove(order.getBookSupplierId());
                        adapter.notifyDataSetChanged();
                        Toast.makeText(v.getContext(),R.string.toast_order_deleted, Toast.LENGTH_SHORT).show();
                    })
                    .setNeutralButton(R.string.no, (dialog, which) -> {
                    });
            builder.create().show();
            return true;
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (getArguments().getBoolean(IS_MAIN_FRAGMENT)) {
            inflater.inflate(R.menu.menu_cart, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_buy:
                return proceedOrder();
            case R.id.action_clear_cart:
                return clearCart();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean proceedOrder() {
        if (cart.isEmpty()){
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.cart_empty_message)
                    .setNeutralButton(R.string.ok,(d,w)->{}).create().show();
            return true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.transaction_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    Intent transactionIntent = IntentsFactory.newTransactionIntent(getActivity());
                    startActivity(transactionIntent);
                })
                .setNeutralButton(R.string.no, (dialog, which) -> {
                });
        builder.create().show();
        return true;
    }

    private boolean clearCart() {
        if (cart.isEmpty()){
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.cart_empty_message)
                    .setNeutralButton(R.string.ok,(d,w)->{}).create().show();
            return true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.clear_cart_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    cart.clear();
                    adapter.notifyDataSetChanged();
                })
                .setNeutralButton(R.string.no, (dialog, which) -> {
                });
        builder.create().show();
        return true;
    }

    @Override
    public int getFragmentTitle() {
        return R.string.cart_fragment_title;
    }
}
