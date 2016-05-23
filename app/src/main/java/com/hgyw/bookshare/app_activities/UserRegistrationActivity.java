package com.hgyw.bookshare.app_activities;

import android.widget.Toast;

import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.exceptions.WrongLoginException;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

import java.text.MessageFormat;

public class UserRegistrationActivity extends UserAbstractActivity {

    public UserRegistrationActivity() {
        super(R.string.register, true, R.string.registration_title);
    }

    // register new user
    public void onOkButton(User user) {
        AccessManager accessManager = AccessManagerFactory.getInstance();
        try {
            accessManager.signUp(user);
            Toast.makeText(this, R.string.registration_Succeed, Toast.LENGTH_SHORT).show();
            startActivity(IntentsFactory.homeIntent(this, true));
        } catch (WrongLoginException e) {
            String message;
            switch (e.getIssue()) {
                case USERNAME_TAKEN:
                    message = getString(R.string.username_taken);
                    break;
                case USERNAME_EMPTY:
                    message = getString(R.string.username_should_not_empty);
                    break;
                default:
                    message = MessageFormat.format("{0}\n{1}: {2}",
                            getString(R.string.registeration_problem_default),
                            getString(R.string.more_details),
                            e.getIssue().getMessage()
                    );
                    break;
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

}



