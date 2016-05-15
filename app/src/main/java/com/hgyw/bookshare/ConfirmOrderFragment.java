package com.hgyw.bookshare;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 *
 * Use the {@link ConfirmOrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfirmOrderFragment extends Fragment {


    public ConfirmOrderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     * @return A new instance of fragment ConfirmOrderFragment.
     */
    public static ConfirmOrderFragment newInstance() {
        ConfirmOrderFragment fragment = new ConfirmOrderFragment();
        Bundle args = new Bundle();
        //future feature
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirm_order, container, false);
    }


}
