package com.hgyw.bookshare.app_activities;


import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Toast;

import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

public class UserEditActivity extends UserAbstractActivity {

    public UserEditActivity() {
        super(R.string.save, false);
    }

    @Override
    public void onOkButton() {
        View rootView = findViewById(android.R.id.content);
        ObjectToViewAppliers.result(rootView, user);
        AccessManagerFactory.getInstance().getGeneralAccess().updateUserDetails(user);
        Toast.makeText(this, R.string.user_details_updated, Toast.LENGTH_LONG).show();
        startActivity(IntentsFactory.homeIntent(this));
    }
}
