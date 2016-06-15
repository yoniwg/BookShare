package com.hgyw.bookshare.app_fragments;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hgyw.bookshare.app_drivers.CancelableLoadingDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ObjectToViewUpdates;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Entity;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;


public class SupplierFragment extends EntityFragment {

    private Activity activity;
    private User supplier;

    public SupplierFragment() {
        super(R.layout.fragment_supplier, 0, R.string.supplier_fragment_title);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        activity = getActivity();
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            new CancelableLoadingDialogAsyncTask<Void, Void, User>(activity) {
                @Override
                protected User retrieveDataAsync(Void... params) {
                    GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
                    supplier = access.retrieve(User.class, entityId);
                    return supplier;
                }

                @Override
                protected void doByData(User supplier) {
                    ObjectToViewAppliers.apply(view, supplier);
                    ObjectToViewUpdates.setListenerToUser(view, supplier);
                }

                @Override
                protected void onCancel() {
                    activity.finish();
                }
            }.execute();
        }

    }

}
