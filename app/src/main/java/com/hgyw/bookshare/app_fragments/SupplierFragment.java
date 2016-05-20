package com.hgyw.bookshare.app_fragments;


import android.os.Bundle;
import android.view.View;

import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;


public class SupplierFragment extends EntityFragment {

    public SupplierFragment() {
        super(R.layout.fragment_supplier, 0, R.string.supplier_fragment_title);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
        Supplier supplier = access.retrieve(Supplier.class, entityId);
        ObjectToViewAppliers.apply(view, supplier);

    }

}
