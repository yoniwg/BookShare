package com.hgyw.bookshare;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;


public class BooksListFragment extends Fragment {
    private final GeneralAccess access;

   public BooksListFragment() {
        this.access = AccessManagerFactory.getInstance().getGeneralAccess();
   }

    public BooksListFragment(GeneralAccess access) {
        this.access = access;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_books_list, container, false);
    }

}
