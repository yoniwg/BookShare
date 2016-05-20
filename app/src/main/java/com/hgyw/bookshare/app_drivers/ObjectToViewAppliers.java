package com.hgyw.bookshare.app_drivers;

import android.content.Context;
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



    public static void apply(View view, Book book) {
        TextView titleView = (TextView) view.findViewById(R.id.bookTitle);
        TextView authorView = (TextView) view.findViewById(R.id.bookAuthor);
        ImageView imageView = (ImageView) view.findViewById(R.id.bookImage);

        if (titleView != null) titleView.setText(book.getTitle());
        if (authorView != null) authorView.setText(book.getAuthor());
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

        if (ratingBar != null) bookReview.setRating(Rating.of((int) ratingBar.getRating()));
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
        TextView priceText = (TextView) view.findViewById(R.id.orderUnitPrice);
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

    public static void apply(View view, Transaction transaction) {
        TextView shippingAddress = (TextView) view.findViewById(R.id.shipping_address);
        TextView creditNumber = (TextView) view.findViewById(R.id.credit_number);
        TextView orderDate = (TextView) view.findViewById(R.id.order_date);

        if (shippingAddress != null) shippingAddress.setText(transaction.getShippingAddress());
        if (creditNumber != null) creditNumber.setText(transaction.getCreditCard());
        if (orderDate != null) orderDate.setText(DateUtils.formatDateTime(null,transaction.getDate().getTime(),
                DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    public static void apply(View view, Order order) {
        TextView amountView = (TextView) view.findViewById(R.id.order_amount);
        TextView finalAmount = (TextView) view.findViewById(R.id.final_amount);
        TextView unitPriceView = (TextView) view.findViewById(R.id.orderUnitPrice);
        TextView totalPriceView = (TextView) view.findViewById(R.id.orderTotalPrice);
        TextView orderStatus = (TextView) view.findViewById(R.id.order_status);
        NumberPicker amountPicker = (NumberPicker) view.findViewById(R.id.orderAmountPicker);

        if (finalAmount != null) finalAmount.setText(String.valueOf(order.getAmount()));
        if (amountView != null) amountView.setText(String.valueOf(order.getAmount()));
        if (unitPriceView != null) unitPriceView.setText(Utility.moneyToString(order.getUnitPrice()));
        if (totalPriceView != null) totalPriceView.setText(Utility.moneyToString(order.calcTotalPrice()));
        if (orderStatus != null) orderStatus.setText(
                Utility.findStringResourceOfEnum(view.getContext(), order.getOrderStatus()) );
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
        TextView firstNameView = (TextView) view.findViewById(R.id.userFirstName);
        TextView lastNameView = (TextView) view.findViewById(R.id.userLastName);
        TextView emailView = (TextView) view.findViewById(R.id.userEmail);
        TextView addressView = (TextView) view.findViewById(R.id.userAddress);
        TextView phoneView = (TextView) view.findViewById(R.id.userPhone);
        DatePicker birthdayView = null;//(DatePicker) view.findViewById(R.id.userBirthday);  // TODO
        ImageView imageView = (ImageView) view.findViewById(R.id.userThumbnail);
        Spinner customerSupplierSpinner = (Spinner) view.findViewById(R.id.customerSupplierSpinner);

        if (firstNameView!= null) firstNameView.setText(user.getFirstName());
        if (lastNameView!= null) lastNameView.setText(user.getLastName());
        if (emailView!= null) emailView.setText(user.getEmail());
        if (addressView!= null) addressView.setText(user.getAddress());
        if (phoneView!= null) phoneView.setText(user.getPhoneNumber());
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
        ImageView imageView = (ImageView) view.findViewById(R.id.userThumbnail);

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

        TextView titleView = (TextView) view.findViewById(R.id.title_query);
        TextView authorView = (TextView) view.findViewById(R.id.author_query);
        // TODO and spinner
        Spinner genreSpinner = (Spinner) view.findViewById(R.id.genre_spinner);
        TextView fromPriceView = (TextView) view.findViewById(R.id.from_price);
        TextView toPriceView = (TextView) view.findViewById(R.id.to_price);

        titleView.setText(bookQuery.getTitleQuery());
        authorView.setText(bookQuery.getAuthorQuery());
        fromPriceView.setText(Utility.moneyToNumberString(bookQuery.getBeginPrice()));
        toPriceView.setText(Utility.moneyToNumberString(bookQuery.getEndPrice()));
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
