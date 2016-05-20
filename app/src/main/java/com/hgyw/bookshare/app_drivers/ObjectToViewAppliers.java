package com.hgyw.bookshare.app_drivers;

import android.support.annotation.IdRes;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookQuery;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSummary;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Credentials;
import com.hgyw.bookshare.entities.Customer;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Rating;
import com.hgyw.bookshare.entities.Supplier;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;

import java.math.BigDecimal;
import java.text.MessageFormat;

/**
 * Created by haim7 on 13/05/2016.
 */
public class ObjectToViewAppliers {

    public static void setTo(View parent, @IdRes int target, String source) {
        TextView textView = (TextView) parent.findViewById(target);
        if (textView != null) textView.setText(source);
    }

    public static void apply(View view, Book book) {
        setTo(view, R.id.bookTitle, book.getTitle());
        setTo(view, R.id.bookAuthor, book.getAuthor());

        ImageView imageView = (ImageView) view.findViewById(R.id.bookImage);
        if (imageView != null) Utility.setImageById(imageView, book.getImageId(), R.drawable.image_book);
    }

    public static void result(View view, Book book) {

        TextView titleView = (TextView) view.findViewById(R.id.bookTitle);
        TextView authorView = (TextView) view.findViewById(R.id.bookAuthor);
        ImageView imageView = (ImageView) view.findViewById(R.id.bookImage); //TODO

        //imageView.getDrawable()

        if (titleView != null) book.setTitle(titleView.getText().toString());
        if (authorView != null) book.setAuthor(authorView.getText().toString());
        //if (imageView != null) Utility.setImageById(imageView, book.getImageId(), R.drawable.image_book);
    }

    public static void apply(View view, BookSummary summary) {
        setTo(view, R.id.priceRange,
                Utility.moneyRangeToString(summary.getMinPrice(), summary.getMaxPrice()));
        setTo(view, R.id.ratingText,
                MessageFormat.format(view.getContext().getString(R.string.num_rates), summary.sumOfRates()));

        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.reviewRating);
        if (ratingBar != null) ratingBar.setRating(summary.clacMeanRating());
    }

    public static void apply(View view, BookReview bookReview) {
        setTo(view, R.id.reviewTitle, bookReview.getTitle());
        setTo(view, R.id.reviewDescription, bookReview.getDescription());

        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.reviewRating);
        if (ratingBar != null) ratingBar.setRating(bookReview.getRating().getStars());
    }

    public static void result(View view, BookReview bookReview) {
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.reviewRating);
        TextView reviewTitleView = (TextView) view.findViewById(R.id.reviewTitle);
        TextView reviewDescriptionView = (TextView) view.findViewById(R.id.reviewDescription);

        if (ratingBar != null) bookReview.setRating(Rating.of((int) ratingBar.getRating()));
        if (reviewTitleView != null) bookReview.setTitle(reviewTitleView.getText().toString());
        if (reviewDescriptionView != null) bookReview.setDescription(reviewDescriptionView.getText().toString());
    }


    public static void apply(View view, Customer reviewer) {
        setTo(view, R.id.customerName, Utility.userNameToString(reviewer));

        ImageView userImage = (ImageView) view.findViewById(R.id.user_thumbnail);
        if (userImage != null) Utility.setImageById(userImage, reviewer.getImageId(), R.drawable.image_user);
    }

    public static void apply(View view, BookSupplier bookSupplier) {
        setTo(view, R.id.order_unit_price, Utility.moneyToString(bookSupplier.getPrice()));
    }

    public static void apply(View view, Supplier supplier) {
        setTo(view, R.id.supplierName, Utility.userNameToString(supplier));
        setTo(view, R.id.supplierAddress, supplier.getAddress());

        ImageView userImage = (ImageView) view.findViewById(R.id.user_thumbnail);
        if (userImage != null) Utility.setImageById(userImage, supplier.getImageId(), R.drawable.image_user);
    }

    public static void apply(View view, Transaction transaction) {
        setTo(view, R.id.shipping_address, transaction.getShippingAddress());
        setTo(view, R.id.credit_number, transaction.getCreditCard());
        setTo(view, R.id.order_date, DateUtils.formatDateTime(null,transaction.getDate().getTime(),
                DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    public static void apply(View view, Order order) {
        setTo(view, R.id.order_amount, String.valueOf(order.getAmount()));
        setTo(view, R.id.final_amount, String.valueOf(order.getAmount()));
        setTo(view, R.id.order_unit_price, String.valueOf(Utility.moneyToString(order.getUnitPrice())) );
        setTo(view, R.id.orderTotalPrice, Utility.moneyToString(order.calcTotalPrice()));
        setTo(view, R.id.order_status, String.valueOf(
                Utility.findStringResourceOfEnum(view.getContext(), order.getOrderStatus())) );

        NumberPicker amountPicker = (NumberPicker) view.findViewById(R.id.orderAmountPicker);
        if (amountPicker != null){
            amountPicker.setMaxValue(100);
            amountPicker.setMinValue(1);
            amountPicker.setValue(order.getAmount());
        }

    }

    public static void apply(View view, Credentials credentials) {
        setTo(view, R.id.username, credentials.getUsername());
        setTo(view, R.id.password, credentials.getPassword());
    }

    /**
     * If the username and  password view was not found, the it will be wmpty string.
     * @param view
     * @return
     */
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
        setTo(view, R.id.userFirstName, user.getFirstName());
        setTo(view, R.id.userLastName, user.getLastName());
        setTo(view, R.id.userEmail, user.getEmail());
        setTo(view, R.id.userAddress, user.getAddress());
        setTo(view, R.id.userPhone, user.getPhoneNumber());

        DatePicker birthdayView = null;//(DatePicker) view.findViewById(R.id.userBirthday);  // TODO
        ImageView imageView = (ImageView) view.findViewById(R.id.user_thumbnail);
        Spinner customerSupplierSpinner = (Spinner) view.findViewById(R.id.customerSupplierSpinner);
        if (birthdayView!= null) {} // TODO
        if (imageView != null) Utility.setImageById(imageView, user.getImageId(), R.drawable.image_user);
        if (customerSupplierSpinner != null) customerSupplierSpinner.setSelection(user.getUserType() == UserType.CUSTOMER ? 0 : 1);
    }

    public static User resultUser(View view) {
        User user;
        Spinner customerSupplierSpinner = (Spinner) view.findViewById(R.id.customerSupplierSpinner);
        if (customerSupplierSpinner != null) {
            user = customerSupplierSpinner.getSelectedItemPosition() == 0 ? new Customer() : new Supplier();
        } else {
            user = new Customer();
        }
        result(view, user);
        return user;
    }

    public static void result(View view, User user) {
        TextView firstNameView = (TextView) view.findViewById(R.id.userFirstName);
        TextView lastNameView = (TextView) view.findViewById(R.id.userLastName);
        TextView emailView = (TextView) view.findViewById(R.id.userEmail);
        TextView addressView = (TextView) view.findViewById(R.id.userAddress);
        TextView phoneView = (TextView) view.findViewById(R.id.userPhone);
        DatePicker birthdayView = null;//(DatePicker) view.findViewById(R.id.userBirthday);  // TODO
        ImageView imageView = (ImageView) view.findViewById(R.id.user_thumbnail);

        user.setCredentials(resultCredentials(view));
        if (firstNameView!= null) user.setFirstName(firstNameView.getText().toString());
        if (lastNameView!= null) user.setLastName(lastNameView.getText().toString());
        if (emailView!= null) user.setEmail(
                emailView.getText().toString());
        if (addressView!= null) user.setAddress(addressView.getText().toString());
        if (phoneView!= null) user.setPhoneNumber(phoneView.getText().toString());
        if (birthdayView!= null) {} // TODO
        if (imageView != null) {}// Do nothing
    }

    public static void apply(View view, BookQuery bookQuery) {
        setTo(view, R.id.title_query, bookQuery.getTitleQuery());
        setTo(view, R.id.author_query, bookQuery.getAuthorQuery());
        setTo(view, R.id.from_price, Utility.moneyToNumberString(bookQuery.getBeginPrice()));
        setTo(view, R.id.to_price, Utility.moneyToNumberString(bookQuery.getEndPrice()));

        // TODO and spinner
        Spinner genreSpinner = (Spinner) view.findViewById(R.id.genre_spinner);
        Book.Genre genreSelection = bookQuery.getGenreSet().isEmpty() ? Book.Genre.GENERAL : bookQuery.getGenreSet().iterator().next();
        genreSpinner.setSelection(genreSelection.ordinal());
    }

    public static BookQuery result(View view, BookQuery bookQuery) {
        TextView titleView = (TextView) view.findViewById(R.id.title_query);
        TextView authorView = (TextView) view.findViewById(R.id.author_query);
        // TODO and spinner
        Spinner genreSpinner = (Spinner) view.findViewById(R.id.genre_spinner);
        TextView fromPriceView = (TextView) view.findViewById(R.id.from_price);
        TextView toPriceView = (TextView) view.findViewById(R.id.to_price);

        bookQuery.setTitleQuery(titleView.getText().toString());
        bookQuery.setAuthorQuery(authorView.getText().toString());
        bookQuery.getGenreSet().clear();
        bookQuery.getGenreSet().add((Book.Genre) genreSpinner.getSelectedItem());
        try {
            bookQuery.setBeginPrice(new BigDecimal(fromPriceView.getText().toString()));
            bookQuery.setEndPrice(new BigDecimal(toPriceView.getText().toString()));
        } catch (NumberFormatException ignored) {}
        return bookQuery;
    }



}
