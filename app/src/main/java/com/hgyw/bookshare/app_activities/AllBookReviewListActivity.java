package com.hgyw.bookshare.app_activities;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ApplyObjectAdapter;
import com.hgyw.bookshare.app_drivers.ObjectToViewUpdates;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManager;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.util.ArrayList;
import java.util.List;

/**
 * A Class represents an extended list of book-reviews
 * should get book id by Intent
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

        //fictive book in order to pass the ID
        Book bookIdContainer = new Book();
        bookIdContainer.setId(bookId);

        //retrieve book-review by async-task
        new AsyncTask<Void,Void,Void>() {
            List<BookReview> bookReviews;
            List<Pair<User,ImageEntity>> customers;
            @Override
            protected Void doInBackground(Void... params) {
                GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
                bookReviews = access.findBookReviews(bookIdContainer);
                customers = new ArrayList<>(bookReviews.size());
                for (BookReview bookReview : bookReviews) {
                    User customer = access.retrieve(User.class, bookReview.getCustomerId());
                    ImageEntity userImage = access.retrieveOptional(ImageEntity.class, customer.getImageId()).orElse(null);
                    customers.add(new Pair<>(customer, userImage));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                ListAdapter listAdapter =
                        new ApplyObjectAdapter<BookReview>(
                                AllBookReviewListActivity.this,
                                R.layout.book_review_component, bookReviews) {
                    @Override
                    protected void applyOnView(View view, int position) {
                        ObjectToViewUpdates.updateBookReviewView(view,
                                bookReviews.get(position),
                                customers.get(position).first,
                                customers.get(position).second);
                    }
                };
                setListAdapter(listAdapter);
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: finish(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }
}
