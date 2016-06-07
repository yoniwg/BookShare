package com.hgyw.bookshare.app_drivers;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

/**
 * Update views by full data of items.
 */
public class ObjectToViewUpdates {

    public static void updateBookReviewView(View view, BookReview bookReview, User customer) {
        ObjectToViewAppliers.apply(view, bookReview);
        ObjectToViewAppliers.apply(view, customer);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.reviewDescription);
        if (descriptionTextView != null) {
            String description = bookReview.getDescription();
            if (description.isEmpty()) descriptionTextView.setVisibility(View.GONE);
        }
    }

    // buyButtonOnClickListener - can be null and the buy-button will be gone.
    public static void updateBookSupplierBuyView(View view, BookSupplier bookSupplier, User supplier, View.OnClickListener buyButtonOnClickListener) {
        ObjectToViewAppliers.apply(view, bookSupplier);
        ObjectToViewAppliers.apply(view, supplier);
        Button buyButton = (Button) view.findViewById(R.id.buy_button);
        if (buyButton != null) {
            if (buyButtonOnClickListener == null) buyButton.setVisibility(View.GONE);
            else buyButton.setOnClickListener(buyButtonOnClickListener);
        }
    }

}
