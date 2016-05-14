package com.hgyw.bookshare;

import android.app.AlertDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.text.style.TtsSpan;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.logicAccess.Cart;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;

/**
 * A fragment representing the cart.
 * <p>
 */
public class CartFragment extends Fragment {

    private CustomerAccess cAccess;

    private MainActivity activity;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CartFragment() {
    }

    public static CartFragment newInstance() {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cAccess = AccessManagerFactory.getInstance().getCustomerAccess();
        activity = (MainActivity) getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListView listView = (ListView) activity.findViewById(R.id.order_list);
        Cart cart = cAccess.getCart();
        List<Order> ordersList = cart.retrieveCartContent();

        ApplyObjectAdapter<Order> adapter = new ApplyObjectAdapter<Order>(activity, R.layout.order_list_item, ordersList) {
            @Override
            protected void applyOnView(View view, int position) {
                Order order = getItem(position);
                ObjectToViewAppliers.apply(view, order);
                BookSupplier bookSupplier = cAccess.retrieve(BookSupplier.class, order.getBookSupplierId());
                ObjectToViewAppliers.apply(view, bookSupplier);
                Book book = cAccess.retrieve(Book.class, bookSupplier.getBookId());
                ObjectToViewAppliers.apply(view, book);
                Supplier supplier = cAccess.retrieve(Supplier.class, bookSupplier.getSupplierId());
                ObjectToViewAppliers.apply(view, supplier);

                NumberPicker orderAmountPicker = (NumberPicker) view.findViewById(R.id.orderAmountPicker);
                orderAmountPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                    order.setAmount(newVal);
                });
            }
        };
        listView.setAdapter(adapter);
        //listView.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, ordersList));

        /*listView.setOnItemClickListener((parent, view, position, id) -> {
            Order order = adapter.getItem(position);

            Toast.makeText(activity, order.shortDescription(), Toast.LENGTH_SHORT).show();
            //startActivity(IntentsFactory.newEntityIntent(activity, order));
        });*/

        /*listView.setOnItemLongClickListener((parent, view, position, id)-> {
                    Order order = adapter.getItem(position);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setView(view)
                            .setMessage(R.string.delete_order_title)
                            .setPositiveButton(R.string.yes, (dialog, which) -> cAccess.getCart().removeFromCart(order))
                            .setNeutralButton(R.string.no, (dialog, which) -> {});
                    builder.create().show();
                    return true;
                }
        );*/
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_cart, menu);
    }
}
