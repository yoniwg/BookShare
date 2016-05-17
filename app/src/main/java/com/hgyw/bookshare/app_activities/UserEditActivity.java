package com.hgyw.bookshare.app_activities;


import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

public class UserEditActivity extends UserAbstractActivity {

    public UserEditActivity() {
        super(R.string.save, false);
    }

    @Override
    public void onOkButton(User user) {
        AccessManagerFactory.getInstance().getGeneralAccess().updateUserDetails(user);
        Toast.makeText(this, R.string.user_details_updated, Toast.LENGTH_LONG).show();
        startActivity(IntentsFactory.homeIntent(this));
    }
}
