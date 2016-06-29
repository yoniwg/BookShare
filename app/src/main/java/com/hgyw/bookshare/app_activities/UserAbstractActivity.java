package com.hgyw.bookshare.app_activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.CancelableLoadingDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.app_drivers.ProgressDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;


public abstract class UserAbstractActivity extends AppCompatActivity {

    private static final String SAVE_KEY_NEW_IMAGE = "newImage";
    private static final String SAVE_KEY_USER = "user";
    public final GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
    private ImageView imageView;
    private User user;
    private final @StringRes int buttonStringId;
    private final boolean isRegistration;
    private Bitmap newImage = null;
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


        View rootView = findViewById(android.R.id.content);
        assert rootView != null;
        imageView = (ImageView) rootView.findViewById(R.id.userThumbnail);
        imageView.setOnClickListener(v -> Utility.startGetImage(this));

        if (savedInstanceState == null) {
            new CancelableLoadingDialogAsyncTask<Pair<User,ImageEntity>>(this) {
                @Override protected Pair<User,ImageEntity> retrieveDataAsync() {
                    User u = isRegistration ? new User() : access.retrieveUserDetails();
                    ImageEntity i = access.retrieveOptional(ImageEntity.class, u.getImageId()).orElse(null);
                    return new Pair<>(u, i);
                }
                @Override protected void doByData(Pair<User,ImageEntity> user) {
                    UserAbstractActivity.this.user = user.first;
                    ObjectToViewAppliers.apply(rootView, user.first);
                    ObjectToViewAppliers.apply(rootView, user.second);
                }

                @Override
                protected void onCancel() {
                    finish();
                }
            }.execute();
        } else {
            newImage = savedInstanceState.getParcelable(SAVE_KEY_NEW_IMAGE);
            user = (User) savedInstanceState.getSerializable(SAVE_KEY_USER);
            if (newImage != null) {
                imageView.setImageBitmap(newImage);
            } else {
                Utility.setImageById(imageView, user.getImageId(), R.drawable.image_user);
            }
        }

        // set username and password not editable
        if (!isRegistration) {
            View[] disabledViews  = {
                    findViewById(R.id.username),
                    findViewById(R.id.password),
                    findViewById(R.id.customerSupplierSpinner)
            };
            for (View v : disabledViews) if (v != null) v.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SAVE_KEY_NEW_IMAGE, newImage);
        outState.putSerializable(SAVE_KEY_USER, user);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_ok, menu);
        menu.findItem(R.id.action_ok).setTitle(buttonStringId);
        return true;
    }

    private void onOkButton() {
        new ProgressDialogAsyncTask<Void>(this) {
            @Override
            protected Void retrieveDataAsync() {
                if (newImage != null) {
                    long imageId = AccessManagerFactory.getInstance().getGeneralAccess().upload(Utility.compress(newImage));
                    if (imageId != 0) {
                        user.setImageId(imageId);
                    } else {
                        Toast.makeText(context, R.string.upload_image_failed, Toast.LENGTH_SHORT).show();
                    }
                }
                return null;
            }

            @Override
            protected void doByData(Void aVoid) {
                View rootView = findViewById(android.R.id.content);
                ObjectToViewAppliers.result(rootView, user);
                onOkButton(user);
            }
        }.execute();
    }

    protected abstract void onOkButton(User user);

    // apply result of apply-image
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        if (requestCode == IntentsFactory.CODE_GET_IMAGE && resultCode == RESULT_OK) {
            newImage = (Bitmap)returnedIntent.getExtras().get("data");
            Utility.setCroppedImageBitmap(imageView, newImage);
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



