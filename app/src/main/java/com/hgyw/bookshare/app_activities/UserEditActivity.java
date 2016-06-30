package com.hgyw.bookshare.app_activities;


import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.utilities.IntentsFactory;
import com.hgyw.bookshare.app_drivers.extensions.ProgressDialogAsyncTask;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

public class UserEditActivity extends UserAbstractActivity {

    public UserEditActivity() {
        super(R.string.save, false, R.string.user_details_title);
    }

    @Override
    public void onOkButton(User user) {
        new ProgressDialogAsyncTask<Void>(this) {
            @Override
            protected Void retrieveDataAsync() {
                AccessManagerFactory.getInstance().getGeneralAccess().updateUserDetails(user); return null;
            }

            @Override
            protected void doByData(Void aVoid) {
                Toast.makeText(context, R.string.user_details_updated, Toast.LENGTH_LONG).show();
                finish();
                startActivity(IntentsFactory.homeIntent(context,true));
            }
        }.execute();
    }
}
