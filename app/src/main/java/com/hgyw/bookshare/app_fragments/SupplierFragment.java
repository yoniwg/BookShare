package com.hgyw.bookshare.app_fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.View;

import com.hgyw.bookshare.app_drivers.CancelableLoadingDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.app_drivers.ObjectToViewUpdates;
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
            new CancelableLoadingDialogAsyncTask<Pair<User,ImageEntity>>(activity) {
                @Override
                protected Pair<User,ImageEntity> retrieveDataAsync() {
                    GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
                    User supplier = access.retrieve(User.class, entityId);
                    ImageEntity userImage = access.retrieveOptional(ImageEntity.class, supplier.getImageId()).orElse(null);
                    return new Pair<>(supplier, userImage);
                }
                @Override
                protected void doByData(Pair<User,ImageEntity> supplier) {
                    ObjectToViewAppliers.apply(view, supplier.first);
                    ObjectToViewAppliers.apply(view, supplier.second);
                    ObjectToViewUpdates.setListenerToUser(view, supplier.first);
                }

                @Override
                protected void onCancel() {
                    activity.finish();
                }
            }.execute();
        }

    }

}
