package com.hgyw.bookshare.app_activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.ObjectToViewAppliers;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.Utility;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.exceptions.WrongLoginException;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int ACTION_IMAGE_CAPTURE_CODE = 0;
    private static final int ACTION_PICK_CODE = 1;
    ImageView userThumbnailImageView;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = getIntent() == null ? null : (User) getIntent().getSerializableExtra(IntentsFactory.ARG_USER_DETAILS);
        if (user == null)
            throw new IllegalArgumentException("The RegistrationActivity should accept non-null user.");

        View rootView = findViewById(android.R.id.content);
        ObjectToViewAppliers.apply(rootView, user);
        assert rootView != null;
        rootView.findViewById(R.id.okButton).setOnClickListener((View.OnClickListener) this);
        userThumbnailImageView = (ImageView) rootView.findViewById(R.id.userThumbnail);
        userThumbnailImageView.setOnClickListener(v -> Utility.startGetImage(this));
    }

    // register new user
    public void onClick(View v) {
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
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}



