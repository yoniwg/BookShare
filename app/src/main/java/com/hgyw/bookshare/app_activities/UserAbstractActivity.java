package com.hgyw.bookshare.app_activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.Utility;
import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.User;

public abstract class UserAbstractActivity extends AppCompatActivity {

    //private static final String ARG_USER_DETAILS = "userDetails";
    ImageView userThumbnailImageView;
    User user;
    private final @StringRes int buttonStringId;
    private final boolean isRegistration;

    protected UserAbstractActivity(@StringRes int buttonStringId, boolean isRegistration) {
        this.buttonStringId = buttonStringId;
        this.isRegistration = isRegistration;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = getIntent() == null ? new Customer() : (User) getIntent().getSerializableExtra(IntentsFactory.ARG_USER_DETAILS);

        View rootView = findViewById(android.R.id.content);
        assert rootView != null;
        ObjectToViewAppliers.apply(rootView, user);
        userThumbnailImageView = (ImageView) rootView.findViewById(R.id.userThumbnail);
        userThumbnailImageView.setOnClickListener(v -> Utility.startGetImage(this));

        // set username and password not editable
        if (!isRegistration) {
            EditText usernameView = (EditText) findViewById(R.id.username);
            EditText passwordView = (EditText) findViewById(R.id.password);
            Spinner customerSupplierSpinner = (Spinner) findViewById(R.id.customerSupplierSpinner);
            if (usernameView != null) usernameView.setVisibility(View.GONE);
            if (passwordView != null) passwordView.setVisibility(View.GONE);
            if (customerSupplierSpinner != null) customerSupplierSpinner.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_details, menu);
        menu.findItem(R.id.action_ok).setTitle(buttonStringId);
        return true;
    }

    public abstract void onOkButton();

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (requestCode == IntentsFactory.GET_IMAGE_CODE && resultCode == RESULT_OK) {
            ImageEntity imageEntity = new ImageEntity();
            Utility.uploadImageURI(this, imageReturnedIntent.getData(), imageEntity, userThumbnailImageView);
            if (imageEntity.getId() == 0) {
                Toast.makeText(this, R.string.upload_image_failed, Toast.LENGTH_SHORT).show();
            } else {
                user.setImageId(imageEntity.getId());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); return true;
            case R.id.okButton:
                onOkButton();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}



