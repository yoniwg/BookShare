package com.hgyw.bookshare.app_activities;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

public class UserEditActivity extends RegistrationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = findViewById(android.R.id.content);
        TextView button = (TextView) rootView.findViewById(R.id.okButton);
        button.setText(R.string.save);
        button.setOnClickListener((View.OnClickListener) this);
        // set username and password not editable
        EditText usernameView = (EditText) findViewById(R.id.username);
        EditText passwordView = (EditText) findViewById(R.id.password);
        usernameView.setKeyListener(null);
        passwordView.setKeyListener(null);
        Spinner customerSupplierSpinner = (Spinner) findViewById(R.id.customerSupplierSpinner);
        customerSupplierSpinner.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        View rootView = findViewById(android.R.id.content);
        ObjectToViewAppliers.result(rootView, user);
        AccessManagerFactory.getInstance().getGeneralAccess().updateUserDetails(user);
        Toast.makeText(this, R.string.user_details_updated, Toast.LENGTH_LONG).show();
        startActivity(IntentsFactory.homeIntent(this));
    }
}
