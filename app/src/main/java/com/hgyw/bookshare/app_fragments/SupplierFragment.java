package com.hgyw.bookshare.app_fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ProgressDialogAsyncTask;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;


public class SupplierFragment extends EntityFragment {

    public SupplierFragment() {
        super(R.layout.fragment_supplier, 0, R.string.supplier_fragment_title);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new ProgressDialogAsyncTask<Void, Void, User>(getActivity()) {
            @Override
            protected User doInBackground1(Void... params) {
                GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
                User supplier = access.retrieve(User.class, entityId);
                return supplier;
            }

            @Override
            protected void onPostExecute1(User supplier) {
                ObjectToViewAppliers.apply(view, supplier);
            }
        }.execute();
    }

}
