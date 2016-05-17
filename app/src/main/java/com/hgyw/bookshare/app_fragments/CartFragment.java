package com.hgyw.bookshare.app_fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.hgyw.bookshare.ApplyObjectAdapter;
import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.logicAccess.Cart;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.logicAccess.CustomerAccess;

import java.util.List;

/**
 * A fragment representing the cart.
 * <p>
 */
public class CartFragment extends AbstractFragment<CustomerAccess> {

    public static final String IS_MAIN_FRAGMENT = "is_amount_can_modify";

    ApplyObjectAdapter<Order> adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CartFragment() {
        super(R.layout.fragment_standard_list, R.menu.menu_cart, R.string.cart_fragment_title);
    }

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

        ListView listView = (ListView) getActivity().findViewById(R.id.list_container);
        Cart cart = access.getCart();
        List<Order> ordersList = cart.retrieveCartContent();

        adapter = new ApplyObjectAdapter<Order>(getActivity(), R.layout.order_list_item, ordersList) {
            @Override
            protected void applyOnView(View view, int position) {
                Order order = getItem(position);
                ObjectToViewAppliers.apply(view, order);
                BookSupplier bookSupplier = access.retrieve(BookSupplier.class, order.getBookSupplierId());
                ObjectToViewAppliers.apply(view, bookSupplier);
                Book book = access.retrieve(Book.class, bookSupplier.getBookId());
                ObjectToViewAppliers.apply(view, book);
                Supplier supplier = access.retrieve(Supplier.class, bookSupplier.getSupplierId());
                ObjectToViewAppliers.apply(view, supplier);
                NumberPicker orderAmountPicker = (NumberPicker) view.findViewById(R.id.orderAmountPicker);
                if (getArguments().getBoolean(IS_MAIN_FRAGMENT)) {
                    orderAmountPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                        order.setAmount(newVal);
                    });
                }else {
                    orderAmountPicker.setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.final_amount).setVisibility(View.VISIBLE);
                }
            }
        };
        listView.setAdapter(adapter);

        //setting delete context menu
        listView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.add(R.string.delete);

            menu.getItem(0).setOnMenuItemClickListener(item -> {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                Order order = (Order) adapter.getItem(info.position);

                //show yes/no alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.delete_order_message)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            access.getCart().remove(order.getBookSupplierId());
                            adapter.notifyDataSetChanged();
                            Toast.makeText(v.getContext(),R.string.toast_order_deleted, Toast.LENGTH_SHORT).show();
                        })
                        .setNeutralButton(R.string.no, (dialog, which) -> {
                        });
                builder.create().show();
                return true;
            });
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getArguments().getBoolean(IS_MAIN_FRAGMENT)) {
            super.onCreateOptionsMenu(menu, inflater);
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
        if (access.getCart().isEmpty()){
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
        if (access.getCart().isEmpty()){
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.cart_empty_message)
                    .setNeutralButton(R.string.ok,(d,w)->{}).create().show();
            return true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.clear_cart_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    access.getCart().clear();
                    adapter.notifyDataSetChanged();
                })
                .setNeutralButton(R.string.no, (dialog, which) -> {
                });
        builder.create().show();
        return true;
    }
}
