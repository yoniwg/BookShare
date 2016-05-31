package com.hgyw.bookshare.app_drivers;

import android.view.View;
import android.widget.TextView;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

/**
 * Update views by full data of items.
 */
public class ObjectToViewUpdates {

    public static void updateBookReviewView(GeneralAccess access, View reviewView, BookReview bookReview) {
        User customer = access.retrieve(User.class, bookReview.getCustomerId());
        ObjectToViewAppliers.apply(reviewView, bookReview);
        ObjectToViewAppliers.apply(reviewView, customer);
        TextView descriptionTextView = (TextView) reviewView.findViewById(R.id.description);
        String description = bookReview.getDescription();
        if (description.isEmpty()) descriptionTextView.setVisibility(View.GONE);

    }



}
