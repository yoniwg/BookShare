package com.hgyw.bookshare;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 *
 * Use the {@link TransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionFragment extends Fragment {


    private CustomerAccess cAccess;

    public TransactionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     * @return A new instance of fragment TransactionFragment.
     */
    public static TransactionFragment newInstance() {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        //future feature
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cAccess = AccessManagerFactory.getInstance().getCustomerAccess();
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Fragment cartFragment = CartFragment.newInstance(false);
        getFragmentManager().beginTransaction()
                .replace(R.id.cart_container, cartFragment)
                .commit();
        //set total sum
        String totalSum = Utility.moneyToNumberString(cAccess.getCart().calculateTotalSum());
        ((TextView)getActivity().findViewById(R.id.total_sum)).setText(totalSum);

        //set listeners to address and credit number
        ((EditText)(getActivity().findViewById(R.id.shipping_address)))
                .addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        cAccess.getCart().getTransaction().setShippingAddress(s.toString());
                    }
                });

        ((EditText)(getActivity().findViewById(R.id.credit_number)))
                .addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        cAccess.getCart().getTransaction().setCreditCard(s.toString());
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_transaction, menu);
    }


}
