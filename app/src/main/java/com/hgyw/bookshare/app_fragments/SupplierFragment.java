package com.hgyw.bookshare.app_fragments;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.hgyw.bookshare.app_drivers.CancelableLoadingDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;


public class SupplierFragment extends EntityFragment {

    private Activity activity;

    public SupplierFragment() {
        super(R.layout.fragment_supplier, 0, R.string.supplier_fragment_title);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        activity = getActivity();
        super.onViewCreated(view, savedInstanceState);
        new CancelableLoadingDialogAsyncTask<Void, Void, User>(activity) {
            @Override
            protected User retrieveDataAsync(Void... params) {
                GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
                User supplier = access.retrieve(User.class, entityId);
                return supplier;
            }

            @Override
            protected void doByData(User supplier) {
                ObjectToViewAppliers.apply(view, supplier);
            }

            @Override
            protected void onCancel() {
                activity.finish();
            }
        }.execute();
    }

}
