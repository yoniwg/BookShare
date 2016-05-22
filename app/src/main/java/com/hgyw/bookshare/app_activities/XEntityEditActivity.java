package com.hgyw.bookshare.app_activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.annimon.stream.function.BiConsumer;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;

import java.io.Serializable;

public abstract class XEntityEditActivity<T extends Serializable> extends AppCompatActivity {/*

    private static final String SAVE_KEY_NEW_IMAGE = "newImage";

    private T item;
    private ImageView imageView;
    private byte[] newImage = null;

    private final @StringRes int buttonStringId;
    private final @StringRes int titleId;
    private final BiConsumer<T, Long> imageIdSetter;
    private final @IdRes int imageViewId;

    protected XEntityEditActivity(@StringRes int buttonStringId, int titleId, int imageViewId, BiConsumer<T, Long> imageIdSetter) {
        this.buttonStringId = buttonStringId;
        this.titleId = titleId;
        this.imageIdSetter = imageIdSetter;
        this.imageViewId = imageViewId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(titleId);

        item = getIntent() == null ? null : (T) getIntent().getSerializableExtra(IntentsFactory.ARG_USER_DETAILS);
        if (item == null) throw new RuntimeException("XEntityEditActivity should accept non-null object.");

        View rootView = findViewById(android.R.id.content);
        assert rootView != null;
        imageView = (ImageView) rootView.findViewById(imageViewId);
        imageView.setOnClickListener(v -> Utility.startGetImage(this));

        setTheView(rootView);
        if (savedInstanceState == null) {
            applyOnNewView(rootView, item);
        } else {
            newImage = savedInstanceState.getByteArray(SAVE_KEY_NEW_IMAGE);
        }
    }

    protected abstract void setTheView(View rootView);
    protected abstract void applyOnNewView(View rootView, T item);
    protected abstract T resultFromView(View rootView, T item);

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray(SAVE_KEY_NEW_IMAGE, newImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_ok, menu);
        menu.findItem(R.id.action_ok).setTitle(buttonStringId);
        return true;
    }

    private void onOkButton() {
        View rootView = findViewById(android.R.id.content);
        item = resultFromView(rootView, item);
        if (newImage != null) {
            long imageId = AccessManagerFactory.getInstance().getGeneralAccess().upload(newImage);
            if (imageId != 0 && imageIdSetter != null) {
                imageIdSetter.accept(item, imageId);
            } else {
                Toast.makeText(this, R.string.upload_image_failed, Toast.LENGTH_SHORT).show();
            }
        }
        onOkButton(item);
    }

    protected abstract void onOkButton(T user);

    // get result of get-image
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        if (requestCode == IntentsFactory.CODE_GET_IMAGE && resultCode == RESULT_OK) {
            newImage = Utility.readImageFromURI(this, returnedIntent.getData(), imageView);
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
    }*/
}



