package com.hgyw.bookshare.app_fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.exceptions.WrongLoginException;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;


public class LoginDialogFragment extends DialogFragment {

    private static final String ARG_DIALOG_CREDENTIALS_OBJECT = "dialogCredentialsObject";
    private static final String PREFERENCE_KEY_USERNAME = "username";

    View view;

    /**
     * Return new instance of this DialogFragment with empty credentials.
     * @return instance of LoginDialogFragment
     */
    public static LoginDialogFragment newInstance() {
        return newInstance(Credentials.EMPTY);
    }

    /**
     * Return new instance of this DialogFragment with saved username.
     * @return instance of LoginDialogFragment
     */
    public static LoginDialogFragment newInstance(Context context) {
        String username = "";
        username = Utility.loadCredentials(context).getUsername();
        return newInstance(new Credentials(username,""));
    }

    /**
     * Return new instance of this DialogFragment with specific credentials.
     * @return instance of LoginDialogFragment
     */
    public static LoginDialogFragment newInstance(Credentials credentials) {
        LoginDialogFragment fragment = new LoginDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIALOG_CREDENTIALS_OBJECT, credentials);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Credentials credentials = getArguments() == null ? null : (Credentials) getArguments().getSerializable(ARG_DIALOG_CREDENTIALS_OBJECT);

        final Activity activity = getActivity();
        view = activity.getLayoutInflater().inflate(R.layout.dialog_login, null);
        if (credentials != null) ObjectToViewAppliers.apply(view, credentials);
        View newAccountButton = view.findViewById(R.id.newAccountView);
        if (newAccountButton != null) newAccountButton.setOnClickListener(v -> {
            startActivity(IntentsFactory.newRegistrationIntent(activity));
            dismiss();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(view)
                .setPositiveButton(R.string.log_in, (dialog, which) -> {
                    Credentials resultCredentials = ObjectToViewAppliers.resultCredentials(view);
                    AccessManager accessManager = AccessManagerFactory.getInstance();
                    try {
                        accessManager.signIn(resultCredentials);;
                    } catch (WrongLoginException e) {
                        int errorMessage;
                        switch (e.getIssue()) {
                            case WRONG_USERNAME_OR_PASSWORD:
                                errorMessage = R.string.wrong_username_or_password; break;
                            default:
                                errorMessage = R.string.error_on_login; break;
                        }
                        new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.wrong_log_in_title)
                                .setMessage(errorMessage)
                                .setPositiveButton(R.string.ok, null)
                                .create().show();
                        return;
                    }
                    Utility.getSharedPreferences(activity).edit()
                            .putString(PREFERENCE_KEY_USERNAME, resultCredentials.getUsername())
                            .commit();
                    startActivity(IntentsFactory.homeIntent(getActivity(), true));
                }).create();
    }

}
