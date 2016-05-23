package com.hgyw.bookshare.app_activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ObjectToViewAppliers;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.SupplierAccess;

/**
 * Throws ClassCastException if the activity cannot supply BookResultListener (by itself or by ListenerSupplier).
 */
public class BookEditActivity extends AppCompatActivity {

    private static final String SAVE_KEY_NEW_IMAGE = "newImage";
    private static final String SAVE_KEY_BOOK = "book";
    private Book book;
    private Bitmap newImage;
    private ImageView imageView;
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get arguments from intent
        IdReference idReference;
        try {
            idReference = IntentsFactory.idReferenceFrom(getIntent().getData());
            if (idReference.getEntityType() != Book.class) {
                throw new IllegalArgumentException("The item should be of entity Book. The given: " + idReference);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Illegal intent. Cause: " + e.getMessage(), e);
        }

        // set the view
        setContentView(R.layout.activity_edit_book);
        imageView = (ImageView) findViewById(R.id.bookImage);
        if (imageView != null) {
            imageView.setOnClickListener(v -> Utility.startGetImage(this));
        }
        Spinner genreSpinner = (Spinner) findViewById(R.id.bookGenreSpinner);
        if (genreSpinner != null) {
            Utility.setSpinnerToEnum(this, genreSpinner, Book.Genre.values());
        }

        // retrieve variables and apply on view
        view = findViewById(android.R.id.content);
        if (savedInstanceState == null) {
            long id = idReference.getId();
            book = id == 0 ? new Book() : AccessManagerFactory.getInstance().getGeneralAccess().retrieve(Book.class, id);
            ObjectToViewAppliers.apply(view, book);
        } else {
            newImage = savedInstanceState.getParcelable(SAVE_KEY_NEW_IMAGE);
            book = (Book) savedInstanceState.getSerializable(SAVE_KEY_BOOK);
            if (newImage != null) {
                imageView.setImageBitmap(newImage);
            } else {
                Utility.setImageById(imageView, book.getImageId());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SAVE_KEY_NEW_IMAGE, newImage);
        outState.putSerializable(SAVE_KEY_BOOK, book);
        super.onSaveInstanceState(outState);
    }

    // get result of get-image
    public void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        if (requestCode == IntentsFactory.CODE_GET_IMAGE && resultCode == RESULT_OK) {
            newImage = Utility.readImageFromURI(this, returnedIntent.getData(), imageView);
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_action_ok, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_ok:
                onActionOk();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onActionOk() {
        ObjectToViewAppliers.result(view, book);
        saveBook();
        setResult(RESULT_OK);
        Toast.makeText(this, R.string.the_book_was_updated, Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onActionOk(View view) {onActionOk();} // for onClick in layout

    private void saveBook() {
        SupplierAccess sAccess = AccessManagerFactory.getInstance().getSupplierAccess();
        if (newImage != null) {
            long imageId = sAccess.upload(Utility.compress(newImage));
            if (imageId != 0) book.setImageId(imageId);
        }
        if (book.getId() == 0) {
            sAccess.addBook(book);
        } else {
            sAccess.updateBook(book);
        }
    }

}
