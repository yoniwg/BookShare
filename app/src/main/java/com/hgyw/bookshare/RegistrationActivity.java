package com.hgyw.bookshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.exceptions.WrongLoginException;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

public class RegistrationActivity extends AppCompatActivity {

    private static final int ACTION_IMAGE_CAPTURE_CODE = 0;
    private static final int ACTION_PICK_CODE = 1;
    ImageView userThumbnailImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        User user = getIntent() == null ? null : (User) getIntent().getSerializableExtra(IntentsFactory.ARG_USER_DETAILS);
        if (user == null) throw new IllegalArgumentException("The RegistrationActivity should accept non-null user.");

        View rootView = findViewById(android.R.id.content);
        ObjectToViewAppliers.apply(rootView, user);
        assert rootView != null;
        rootView.findViewById(R.id.registrationButton).setOnClickListener(v -> this.onRegistration());
        userThumbnailImageView = (ImageView) rootView.findViewById(R.id.userThumbnail);
        userThumbnailImageView.setOnClickListener(v -> startGetImage());
    }

    private void onRegistration() {
        View rootView = findViewById(android.R.id.content);
        User user = new Customer(); //TODO
        Context context = this;

        ObjectToViewAppliers.result(rootView, user);
        AccessManager accessManager = AccessManagerFactory.getInstance();
        try {
            accessManager.signUp(user);
            Toast.makeText(this, R.string.registration_Succeed, Toast.LENGTH_SHORT).show();
            startActivity(IntentsFactory.homeIntent(context));
        } catch (WrongLoginException e) {
            Toast.makeText(context, "Registration was not succeed: " + e.getIssue(), Toast.LENGTH_LONG).show(); // TODO
        }
    }

    private void startGetImage() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose)
                .setMessage("Check way to bring image.")
                .setPositiveButton("CAPTURE", (dialog, which) -> {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, ACTION_IMAGE_CAPTURE_CODE);
                })
                .setNegativeButton("PICK", (dialog, which) -> {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , ACTION_PICK_CODE);
                })
                .create().show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case ACTION_IMAGE_CAPTURE_CODE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    userThumbnailImageView.setImageURI(selectedImage);
                }
                break;
            case ACTION_PICK_CODE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    userThumbnailImageView.setImageURI(selectedImage);
                }
                break;
        }
    }

}
