package com.hgyw.bookshare;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.exceptions.WrongLoginException;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;


public class LoginDialogFragment extends DialogFragment {

    private static final String ARG_DIALOG_CREDENTIALS_OBJECT = "dialogCredentialsObject";

    View view;

    public static LoginDialogFragment newInstance() {
        return newInstance(Credentials.create("",""));
    }

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

        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_login, null);
        if (credentials != null) ObjectToViewAppliers.apply(view, credentials);
        View newAccountButton = view.findViewById(R.id.newAccountView);
        if (newAccountButton != null) newAccountButton.setOnClickListener(v -> {
            View newAccountView = getActivity().getLayoutInflater().inflate(R.layout.user_simple_dialog_component, null);
            new AlertDialog.Builder(getActivity()).setView(newAccountView)
                    .setPositiveButton(R.string.register, (dialog, which) -> {
                        Customer customer = new Customer();
                        ObjectToViewAppliers.result(newAccountView, customer);
                        AccessManager accessManager = AccessManagerFactory.getInstance();
                        try {
                            accessManager.signUp(customer);
                            IntentsFactory.afterLoginIntent(getActivity());
                        } catch (WrongLoginException e) {
                            Toast.makeText(getActivity(), "Login eas not succes: " + e.getIssue(), Toast.LENGTH_LONG).show(); // TODO
                        }

                    })
                    .create().show();
            dismiss();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(view)
                .setPositiveButton(R.string.log_in, (dialog, which) -> {
                    Credentials resultCredentials = ObjectToViewAppliers.resultCredentials(view);
                    AccessManager accessManager = AccessManagerFactory.getInstance();
                    try {
                        accessManager.signIn(resultCredentials);
                        ((MainActivity)getActivity()).updateDrawerOnLogin();
                        startActivity(IntentsFactory.afterLoginIntent(getActivity()));
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
                    }
                }).create();
    }

}