package com.hgyw.bookshare.app_fragments;


import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;


public class SupplierFragment extends EntityFragment {

    public SupplierFragment() {
        super(R.layout.fragment_supplier, 0);
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
