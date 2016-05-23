package com.hgyw.bookshare.app_activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewUpdates;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.List;

/**
 * Created by haim7 on 23/05/2016.
 */
public class AllBookReviewListActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().setDisplayHomeAsUpEnabled(true); TODO ActionBar

        // read book id from uri
        String bookIdString = getIntent().getData().getPath().replaceAll("\\D+","");
        long bookId = Long.parseLong(bookIdString);

        GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
        Book book = new Book(); book.setId(bookId);
        List<BookReview> bookReviews = access.findBookReviews(book);
        setListAdapter(new ApplyObjectAdapter<BookReview>(this, R.layout.book_review_component, bookReviews) {
            @Override
            protected void applyOnView(View view, int position) {
                BookReview bookReview = getItem(position);
                ObjectToViewUpdates.updateBookReviewView(access, view, bookReview);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: finish(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
