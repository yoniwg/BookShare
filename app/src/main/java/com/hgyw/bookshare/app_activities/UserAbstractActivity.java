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
import android.widget.Toast;

import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

public abstract class UserAbstractActivity extends AppCompatActivity {

    //private static final String ARG_USER_DETAILS = "userDetails";
    private ImageView userThumbnailImageView;
    private User user;
    private final @StringRes int buttonStringId;
    private final boolean isRegistration;
    private byte[] newImage = null;
    private final @StringRes int titleId;

    protected UserAbstractActivity(@StringRes int buttonStringId, boolean isRegistration, int titleId) {
        this.buttonStringId = buttonStringId;
        this.isRegistration = isRegistration;
        this.titleId = titleId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(titleId);
        user = getIntent() == null ? null : (User) getIntent().getSerializableExtra(IntentsFactory.ARG_USER_DETAILS);
        if (user == null) user = new Customer();

        View rootView = findViewById(android.R.id.content);
        assert rootView != null;
        ObjectToViewAppliers.apply(rootView, user);
        userThumbnailImageView = (ImageView) rootView.findViewById(R.id.user_thumbnail);
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

    private void onOkButton() {
        if (newImage != null) {
            long imageId = AccessManagerFactory.getInstance().getGeneralAccess().upload(newImage);
            if (imageId != 0) {
                user.setImageId(imageId);
            } else {
                Toast.makeText(this, R.string.upload_image_failed, Toast.LENGTH_SHORT).show();
            }
        }
        View rootView = findViewById(android.R.id.content);
        ObjectToViewAppliers.result(rootView, user);
        onOkButton(user);
    }

    protected abstract void onOkButton(User user);

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (requestCode == IntentsFactory.GET_IMAGE_CODE && resultCode == RESULT_OK) {
            newImage = Utility.readImageFromURI(this, imageReturnedIntent.getData(), userThumbnailImageView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); return true;
            case R.id.action_ok:
                onOkButton(); return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}



