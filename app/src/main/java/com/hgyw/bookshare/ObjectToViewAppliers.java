package com.hgyw.bookshare;

import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;

import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Rating;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.entities.User;

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
        if (imageView != null) Utility.setImageById(imageView, book.getImageId(), R.drawable.image_book);
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
        TextView customerNameView = (TextView) view.findViewById(R.id.customerName);
        ImageView userImage = (ImageView) view.findViewById(R.id.userThumbnail);

        if (customerNameView != null) customerNameView.setText(Utility.userNameToString(reviewer));
        if (userImage != null) Utility.setImageById(userImage, reviewer.getImageId(), R.drawable.image_user);
    }

    public static void apply(View view, BookSupplier bookSupplier) {
        TextView priceText = (TextView) view.findViewById(R.id.price);
        if (bookSupplier != null) priceText.setText(Utility.moneyToString(bookSupplier.getPrice()));
    }

    public static void apply(View view, Supplier supplier) {
        TextView supplierNameView = (TextView) view.findViewById(R.id.supplierName);
        TextView supplierAddressView = (TextView) view.findViewById(R.id.supplierAddress);
        ImageView userImage = (ImageView) view.findViewById(R.id.userThumbnail);

        if (supplierNameView != null) supplierNameView.setText(Utility.userNameToString(supplier));
        if (supplierAddressView != null) supplierAddressView.setText(supplier.getAddress());
        if (userImage != null) Utility.setImageById(userImage, supplier.getImageId(), R.drawable.image_user);
    }

    public static void apply(View view, Order order) {
        TextView amountView = (TextView) view.findViewById(R.id.orderAmount);
        TextView finalAmount = (TextView) view.findViewById(R.id.final_amount);
        TextView unitPriceView = (TextView) view.findViewById(R.id.orderUnitPrice);
        TextView totalPriceView = (TextView) view.findViewById(R.id.orderTotalPrice);
        NumberPicker amountPicker = (NumberPicker) view.findViewById(R.id.orderAmountPicker);

        if (finalAmount != null) finalAmount.setText(String.valueOf(order.getAmount()));
        if (amountView != null) amountView.setText(String.valueOf(order.getAmount()));
        if (unitPriceView != null) unitPriceView.setText(Utility.moneyToString(order.getUnitPrice()));
        if (totalPriceView != null) totalPriceView.setText(Utility.moneyToString(order.calcTotalPrice()));
        if (amountPicker != null){
            amountPicker.setMaxValue(100);
            amountPicker.setMinValue(1);
            amountPicker.setValue(order.getAmount());
        }

    }

    public static void apply(View view, Credentials credentials) {
        TextView usernameView = (TextView) view.findViewById(R.id.username);
        TextView passwordView = (TextView) view.findViewById(R.id.password);

        if (usernameView != null) usernameView.setText(credentials.getUsername());
        if (passwordView != null) passwordView.setText(credentials.getPassword());
    }

    public static Credentials resultCredentials(View view) {
        TextView usernameView = (TextView) view.findViewById(R.id.username);
        TextView passwordView = (TextView) view.findViewById(R.id.password);

        String username = "";
        String password = "";
        if (usernameView != null) username = usernameView.getText().toString();
        if (passwordView != null) password = passwordView.getText().toString();
        return Credentials.create(username, password);
    }

    public static void apply(View view, User user) {
        apply(view, user.getCredentials());
        TextView firstNameView = (TextView) view.findViewById(R.id.userFirstName);
        TextView lastNameView = (TextView) view.findViewById(R.id.userLastName);
        TextView addressView = (TextView) view.findViewById(R.id.userAddress);
        TextView phoneView = (TextView) view.findViewById(R.id.userPhone);
        DatePicker birthdayView = (DatePicker) view.findViewById(R.id.userBirthday);
        ImageView imageView = (ImageView) view.findViewById(R.id.userThumbnail);

        if (firstNameView!= null) firstNameView.setText(user.getFirstName());
        if (lastNameView!= null) lastNameView.setText(user.getLastName());
        if (addressView!= null) lastNameView.setText(user.getAddress());
        if (phoneView!= null) lastNameView.setText(user.getPhoneNumber());
        if (birthdayView!= null) {} // TODO
        if (imageView != null) Utility.setImageById(imageView, user.getImageId(), R.drawable.image_user);
    }

    public static void result(View view, User user) {
        apply(view, user.getCredentials());
        TextView firstNameView = (TextView) view.findViewById(R.id.userFirstName);
        TextView lastNameView = (TextView) view.findViewById(R.id.userLastName);
        TextView addressView = (TextView) view.findViewById(R.id.userAddress);
        TextView phoneView = (TextView) view.findViewById(R.id.userPhone);
        DatePicker birthdayView = (DatePicker) view.findViewById(R.id.userBirthday);
        ImageView imageView = (ImageView) view.findViewById(R.id.userThumbnail);

        if (firstNameView!= null) user.setFirstName(firstNameView.getText().toString());
        if (lastNameView!= null) user.setLastName(lastNameView.getText().toString());
        if (addressView!= null) user.setAddress(addressView.getText().toString());
        if (phoneView!= null) user.setPhoneNumber(phoneView.getText().toString());
        if (birthdayView!= null) {} // TODO
        if (imageView != null) {}// TODO

    }


    }
