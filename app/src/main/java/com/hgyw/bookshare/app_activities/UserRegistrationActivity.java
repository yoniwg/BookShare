package com.hgyw.bookshare.app_activities;

import android.view.View;
import android.widget.Toast;

import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.exceptions.WrongLoginException;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

public class UserRegistrationActivity extends UserAbstractActivity {

    public UserRegistrationActivity() {
        super(R.string.register, true, R.string.registration_title);
    }

    // register new user
    public void onOkButton() {
        View rootView = findViewById(android.R.id.content);
        ObjectToViewAppliers.result(rootView, user);
        AccessManager accessManager = AccessManagerFactory.getInstance();
        try {
            accessManager.signUp(user);
            Toast.makeText(this, R.string.registration_Succeed, Toast.LENGTH_SHORT).show();
            startActivity(IntentsFactory.homeIntent(this, true));
        } catch (WrongLoginException e) {
            Toast.makeText(this, "Registration was not succeed: " + e.getIssue(), Toast.LENGTH_LONG).show(); // TODO
        }
    }

}



