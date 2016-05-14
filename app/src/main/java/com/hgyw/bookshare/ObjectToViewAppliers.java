package com.hgyw.bookshare;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Rating;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.text.MessageFormat;

/**
 * Created by haim7 on 13/05/2016.
 */
public class ObjectToViewAppliers {



    public static void apply(View view, Book book) {
        TextView titleView = (TextView) view.findViewById(R.id.bookTitle);
        TextView authorView = (TextView) view.findViewById(R.id.bookAuthor);
        ImageView imageView = (ImageView) view.findViewById(R.id.bookImage);

        if (titleView != null) titleView.setText(book.getTitle());
        if (authorView != null) authorView.setText(book.getAuthor());
        if (imageView != null) Utility.setImageById(imageView, book.getImageId());
    }

    public static void apply(View view, BookSummary summary) {
        Context context = view.getContext();

        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.reviewRating);
        TextView priceView = (TextView) view.findViewById(R.id.priceRange);
        TextView ratingTextView = (TextView) view.findViewById(R.id.ratingText);

        if (ratingBar != null) ratingBar.setRating(summary.clacMeanRating());
        if (priceView != null) priceView.setText(Utility.moneyRangeToString(summary.getMinPrice(), summary.getMaxPrice()));
        if (ratingTextView != null) ratingTextView.setText(MessageFormat.format(context.getString(R.string.num_rates), summary.sumOfRates()));
    }

    public static void apply(View view, BookReview bookReview) {
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.reviewRating);
        TextView reviewTitleView = (TextView) view.findViewById(R.id.reviewTitle);
        TextView reviewDescriptionView = (TextView) view.findViewById(R.id.reviewDescription);

        if (ratingBar != null) ratingBar.setRating(bookReview.getRating().getStars());
        if (reviewTitleView != null) reviewTitleView.setText(bookReview.getTitle());
        if (reviewDescriptionView != null) reviewDescriptionView.setText(bookReview.getDescription());
    }

    public static void result(View view, BookReview bookReview) {
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.reviewRating);
        TextView reviewTitleView = (TextView) view.findViewById(R.id.reviewTitle);
        TextView reviewDescriptionView = (TextView) view.findViewById(R.id.reviewDescription);

        if (ratingBar != null) bookReview.setRating(Rating.ofStars((int) ratingBar.getRating()));
        if (reviewTitleView != null) bookReview.setTitle(reviewTitleView.getText().toString());
        if (reviewDescriptionView != null) bookReview.setDescription(reviewDescriptionView.getText().toString());
    }


    public static void apply(View view, Customer reviewer) {
        TextView usernameView = (TextView) view.findViewById(R.id.username);
        ImageView userImage = (ImageView) view.findViewById(R.id.userThumbnail);

        if (usernameView != null) usernameView.setText(Utility.usernameToString(reviewer));
        if (userImage != null) Utility.setImageById(userImage, reviewer.getImageId());
    }

    public static void apply(View view, BookSupplier bookSupplier) {
        TextView priceText = (TextView) view.findViewById(R.id.price);
        if (bookSupplier != null) priceText.setText(Utility.moneyToString(bookSupplier.getPrice()));
    }

    public static void apply(View view, Supplier supplier) {
        TextView supplierNameText = (TextView) view.findViewById(R.id.supplierName);
        TextView supplierAddress = (TextView) view.findViewById(R.id.supplierAddress);
        ImageView userImage = (ImageView) view.findViewById(R.id.userThumbnail);

        if (supplierNameText != null) supplierNameText.setText(Utility.usernameToString(supplier));
        if (supplierAddress != null) supplierAddress.setText(supplier.getAddress());
        if (userImage != null) Utility.setImageById(userImage, supplier.getImageId());
    }

    public static void apply(View view, Order order) {
        TextView amountText = (TextView) view.findViewById(R.id.orderAmount);
        TextView unitPriceText = (TextView) view.findViewById(R.id.orderUnitPrice);
        TextView totalPriceText = (TextView) view.findViewById(R.id.orderTotalPrice);
        NumberPicker amountPicker = (NumberPicker) view.findViewById(R.id.orderAmountPicker);

        if (amountText != null) amountText.setText(String.valueOf(order.getAmount()));
        if (unitPriceText != null) unitPriceText.setText(Utility.moneyToString(order.getUnitPrice()));
        if (totalPriceText != null) totalPriceText.setText(Utility.moneyToString(order.calcTotalPrice()));
        if (amountPicker != null){
            amountPicker.setMaxValue(100);
            amountPicker.setMinValue(1);
            amountPicker.setValue(order.getAmount());
        }
    }


}
