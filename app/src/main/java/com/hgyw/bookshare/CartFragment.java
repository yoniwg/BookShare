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
import android.widget.Toast;

import com.hgyw.bookshare.logicAccess.Cart;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;

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
                ObjectToViewAppliers.applyOrder(view, order);
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Order order = adapter.getItem(position);

            Toast.makeText(activity, order.shortDescription(), Toast.LENGTH_SHORT).show();
            EntityActivity.startNewActivity(activity, order.getEntityType(), order.getId());
        });

        listView.setOnItemLongClickListener((parent, view, position, id)-> {
                    Order order = adapter.getItem(position);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setView(view)
                            .setMessage(R.string.delete_order_title)
                            .setNegativeButton(R.string.yes, (dialog, which) -> cAccess.getCart().removeFromCart(order))
                            .setPositiveButton(R.string.no, (dialog, which) -> {
                            });
                    builder.create().show();
                    return true;
                }
        );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_cart, menu);
    }
}
