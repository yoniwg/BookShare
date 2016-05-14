package com.hgyw.bookshare;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;


public class SupplierFragment extends EntityFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_supplier, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
        Supplier supplier = access.retrieve(Supplier.class, entityId);
        ObjectToViewAppliers.apply(view, supplier);

    }

    @Override
    public int getTitleResource() {
        return R.string.supplier_fragment_title;
    }
}
