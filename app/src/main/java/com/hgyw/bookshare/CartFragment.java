package com.hgyw.bookshare;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.logicAccess.Cart;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;

/**
 * A fragment representing the cart.
 * <p>
 */
public class CartFragment extends Fragment {

    private GeneralAccess access;

    private MainActivity activity;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CartFragment() {
    }

    public static CartFragment newInstance(Cart cart) {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        args.putSerializable(IntentsFactory.ARG_CART, cart);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        access = AccessManagerFactory.getInstance().getCustomerAccess();
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

        List<Long> ordersIdList = (List<Long>) getArguments().get(IntentsFactory.ARG_CART);
        List<Order> ordersList = Stream.of(ordersIdList).map(oid -> access.retrieve(Order.class, oid)).collect(Collectors.toList());
        ArrayAdapter<Order> arrayAdapter = new ArrayAdapter<Order>(activity, android.R.layout.simple_list_item_1, ordersList);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Order order = arrayAdapter.getItem(position);

            Toast.makeText(activity, order.shortDescription(), Toast.LENGTH_SHORT).show();
            EntityActivity.startNewActivity(activity, order.getEntityType(), order.getId());
        });
    }
}
