package com.hgyw.bookshare;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.exceptions.WrongLoginException;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

import org.w3c.dom.Text;


public class CredentialsDialogFragment extends DialogFragment {

    private static final String ARG_DIALOG_CREDENTIALS_OBJECT = "dialogCredentialsObject";

    View view;

    public static CredentialsDialogFragment newInstance() {
        return newInstance(Credentials.create("",""));
    }

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
                        String userName = resultCredentials.getUsername();
                        ((TextView) getActivity().findViewById(R.id.drawer_user_name)).setText(userName);

                        long userImageId = accessManager.getGeneralAccess().retrieveUserDetails().getImageId();
                        Utility.setImageById((ImageView) getActivity().findViewById(R.id.drwer_user_image), userImageId);
                        ((NavigationView)getActivity().findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_login).setVisible(false);
                        ((NavigationView)getActivity().findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_logout).setVisible(true);
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
