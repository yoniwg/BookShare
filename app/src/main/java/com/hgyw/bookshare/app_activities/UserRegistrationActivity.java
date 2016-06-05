package com.hgyw.bookshare.app_activities;

import android.widget.Toast;

import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ProgressDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.Utility;
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
        new ProgressDialogAsyncTask<Void,Void,WrongLoginException>(this, R.string.registering) {
            @Override
            protected WrongLoginException doInBackground1(Void... params) {
                try {
                    accessManager.signUp(user);
                    return null;
                } catch (WrongLoginException e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute1(WrongLoginException e) {
                if (e == null) {
                    Toast.makeText(context, R.string.registration_Succeed, Toast.LENGTH_SHORT).show();
                    Utility.saveCredentials(context, user.getCredentials());
                    startActivity(IntentsFactory.homeIntent(context, true));
                } else {
                    String message;
                    switch (e.getIssue()) {
                        case USERNAME_TAKEN:
                            message = getString(R.string.username_taken); break;
                        case USERNAME_EMPTY:
                            message = getString(R.string.username_should_not_empty); break;
                        default:
                            message = MessageFormat.format("{0}\n{1}: {2}",
                                    getString(R.string.registeration_problem_default),
                                    getString(R.string.more_details),
                                    e.getIssue().getMessage()
                            ); break;
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

}



