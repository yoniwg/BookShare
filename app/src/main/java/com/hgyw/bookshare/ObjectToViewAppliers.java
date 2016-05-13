package com.hgyw.bookshare;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.Supplier;

import java.text.MessageFormat;

/**
 * Created by haim7 on 13/05/2016.
 */
public class ObjectToViewAppliers {
    public static void applyBook(View view, Book book, BookSummary summary) {
        Context context = view.getContext();

        TextView titleView = (TextView) view.findViewById(R.id.bookTitle);
        TextView authorView = (TextView) view.findViewById(R.id.bookAuthor);
        ImageView imageView = (ImageView) view.findViewById(R.id.bookImage);
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        TextView priceView = (TextView) view.findViewById(R.id.priceRange);
        TextView ratingTextView = (TextView) view.findViewById(R.id.ratingText);

        if (titleView != null) titleView.setText(book.getTitle());
        if (authorView != null) authorView.setText(book.getAuthor());
        if (imageView != null) Utility.setImageById(imageView, book.getImageId());
        if (ratingBar != null) ratingBar.setRating(summary.clacMeanRating());
        if (priceView != null) priceView.setText(Utility.moneyRangeToString(summary.getMinPrice(), summary.getMaxPrice()));
        if (ratingTextView != null) ratingTextView.setText(MessageFormat.format(context.getString(R.string.num_rates), summary.sumOfRates()));
    }

    public static void applyBookReview(View view, BookReview bookReview, Customer reviewer) {
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        TextView reviewTitleView = (TextView) view.findViewById(R.id.reviewTitle);
        TextView reviewDescriptionView = (TextView) view.findViewById(R.id.reviewDescription);
        TextView usernameView = (TextView) view.findViewById(R.id.username);
        ImageView userImage = (ImageView) view.findViewById(R.id.userThumbnail);

        if (ratingBar != null) ratingBar.setRating(bookReview.getRating().getStars());
        if (reviewTitleView != null) reviewTitleView.setText(bookReview.getTitle());
        if (reviewDescriptionView != null) reviewDescriptionView.setText(bookReview.getDescription());
        if (usernameView != null) usernameView.setText(Utility.usernameToString(reviewer));
        if (userImage != null) Utility.setImageById(userImage, reviewer.getImageId());
    }

    public static void applyBookSupplier(View view, BookSupplier bookSupplier, Supplier supplier) {
        TextView supplierNameText = (TextView) view.findViewById(R.id.supplierName);
        TextView priceText = (TextView) view.findViewById(R.id.price);
        ImageView userImage = (ImageView) view.findViewById(R.id.userThumbnail);
        if (supplierNameText != null) supplierNameText.setText(Utility.usernameToString(supplier));
        if (bookSupplier != null) priceText.setText(Utility.moneyToString(bookSupplier.getPrice()));
        if (userImage != null) Utility.setImageById(userImage, supplier.getImageId());
    }
}
