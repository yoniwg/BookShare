package com.hgyw.bookshare;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.exceptions.WrongLoginException;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;


public class CredentialsDialogFragment extends DialogFragment {

    private static final String ARG_DIALOG_CREDENTIALS_OBJECT = "dialogCredentialsObject";

    View view;

    public static CredentialsDialogFragment newInstance(Credentials credentials) {
        CredentialsDialogFragment fragment = new CredentialsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIALOG_CREDENTIALS_OBJECT, credentials);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Credentials credentials = getArguments() == null ? null : (Credentials) getArguments().getSerializable(ARG_DIALOG_CREDENTIALS_OBJECT);

        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_credentials, null);
        if (credentials != null) ObjectToViewAppliers.apply(view, credentials);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(view)
                .setPositiveButton(R.string.log_in, (dialog, which) -> {
                    Credentials resultCredentials = ObjectToViewAppliers.resultCredentials(view);
                    AccessManager accessManager = AccessManagerFactory.getInstance();
                    try {
                        accessManager.signIn(resultCredentials);
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
                        startActivity(IntentsFactory.afterLoginIntent(getActivity()));
                    }
                }).create();
    }

}
