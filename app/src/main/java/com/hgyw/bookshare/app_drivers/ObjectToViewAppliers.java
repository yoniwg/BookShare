package com.hgyw.bookshare.app_drivers;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.Rating;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Set;

/**
 * Created by haim7 on 13/05/2016.
 */
public class ObjectToViewAppliers {

    public static void apply(View view, Book book) {
        TextView titleView = (TextView) view.findViewById(R.id.bookTitle);
        TextView authorView = (TextView) view.findViewById(R.id.bookAuthor);
        TextView genreView = (TextView) view.findViewById(R.id.bookGenre);
        ImageView imageView = (ImageView) view.findViewById(R.id.bookImage);
        Spinner genreSpinner = (Spinner)  view.findViewById(R.id.bookGenreSpinner);
        if (titleView != null) titleView.setText(book.getTitle());
        if (authorView != null) authorView.setText(book.getAuthor());
        if (genreView != null) genreView.setText(book.getAuthor());
        if (genreSpinner != null) genreSpinner.setSelection(book.getGenre().ordinal());
    }

    public static void result(View view, Book book) {
        TextView titleView = (TextView) view.findViewById(R.id.bookTitle);
        TextView authorView = (TextView) view.findViewById(R.id.bookAuthor);
        TextView genreView = (TextView) view.findViewById(R.id.bookGenre);
        ImageView imageView = (ImageView) view.findViewById(R.id.bookImage);
        Spinner genreSpinner = (Spinner)  view.findViewById(R.id.bookGenreSpinner);

        //imageView.getDrawable()

        if (titleView != null) book.setTitle(titleView.getText().toString());
        if (authorView != null) book.setAuthor(authorView.getText().toString());
        //if (imageView != null) Utility.setImageById(imageView, book.getImageId(), R.drawable.image_book);
        if (genreSpinner != null) book.setGenre((Book.Genre) genreSpinner.getSelectedItem());
    }

    public static void apply(View view, BookSummary summary) {
        Context context = view.getContext();

        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.reviewRating);
        TextView priceView = (TextView) view.findViewById(R.id.priceRange);
        TextView ratingTextView = (TextView) view.findViewById(R.id.ratingText);

        if (ratingBar != null) ratingBar.setRating(summary.calcMeanRating());
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



    public static void apply(View view, BookSupplier bookSupplier) {
        TextView priceText = (TextView) view.findViewById(R.id.bookSupplierPrice);
        TextView amountText = (TextView) view.findViewById(R.id.bookSupplierAmount);

        if (priceText != null) priceText.setText(Utility.moneyToNumberString(bookSupplier.getPrice()));
        if (amountText != null) amountText.setText(String.valueOf(bookSupplier.getAmountAvailable()));
    }

    public static void result(View view, BookSupplier bookSupplier) {
        TextView priceText = (TextView) view.findViewById(R.id.bookSupplierPrice);
        TextView amountText = (TextView) view.findViewById(R.id.bookSupplierAmount);

        if (priceText != null) try {
            bookSupplier.setPrice(new BigDecimal(priceText.getText().toString()));
        } catch (NumberFormatException ignored) {}
        if (amountText != null) try {
            bookSupplier.setAmountAvailable(Integer.parseInt(amountText.getText().toString()));
        } catch (NumberFormatException ignored) {}
    }

    public static void apply(View view, Order order) {
        TextView finalAmountView = (TextView) view.findViewById(R.id.orderAmountFinal);
        TextView unitPriceView = (TextView) view.findViewById(R.id.orderUnitPrice);
        TextView totalPriceView = (TextView) view.findViewById(R.id.orderTotalPrice);
        NumberPicker amountPicker = (NumberPicker) view.findViewById(R.id.orderAmountPicker);
        TextView amountTextView = (TextView) view.findViewById(R.id.amountTextView);
        TextView orderStatus = (TextView) view.findViewById(R.id.orderStatus);

        if (finalAmountView != null) finalAmountView.setText(String.valueOf(order.getAmount()));
        if (unitPriceView != null) unitPriceView.setText(Utility.moneyToString(order.getUnitPrice()));
        if (totalPriceView != null) totalPriceView.setText(Utility.moneyToString(order.calcTotalPrice()));
        if (amountPicker != null){
            amountPicker.setMaxValue(100);
            amountPicker.setMinValue(1);
            amountPicker.setValue(order.getAmount());
        }
        if (amountTextView != null) amountTextView.setText(String.valueOf(order.getAmount()));
        if (orderStatus != null) orderStatus.setText(Utility.findStringResourceOfEnum(view.getContext(), order.getOrderStatus()));
    }

    public static void apply(View view, Credentials credentials) {
        TextView usernameView = (TextView) view.findViewById(R.id.username);
        TextView passwordView = (TextView) view.findViewById(R.id.password);

        if (usernameView != null) usernameView.setText(credentials.getUsername());
        if (passwordView != null) passwordView.setText(credentials.getPassword());
    }

    /**
     * If the username and password view was not found, the it will be empty string.
     */
    public static Credentials resultCredentials(View view) {
        TextView usernameView = (TextView) view.findViewById(R.id.username);
        TextView passwordView = (TextView) view.findViewById(R.id.password);

        String username = "";
        String password = "";
        if (usernameView != null) username = usernameView.getText().toString();
        if (passwordView != null) password = passwordView.getText().toString();
        return new Credentials(username, password);
    }


    public static void apply(View view, User user) {
        apply(view, user.getCredentials());
        TextView firstNameView = (TextView) view.findViewById(R.id.userFirstName);
        TextView lastNameView = (TextView) view.findViewById(R.id.userLastName);
        TextView fullNameView = (TextView) view.findViewById(R.id.userFullName);
        TextView emailView = (TextView) view.findViewById(R.id.userEmail);
        TextView addressView = (TextView) view.findViewById(R.id.userAddress);
        TextView phoneView = (TextView) view.findViewById(R.id.userPhone);
        DatePicker birthdayView = null;//(DatePicker) view.findViewById(R.id.userBirthday);  // TODO
        ImageView userImage = (ImageView) view.findViewById(R.id.userThumbnail);
        Spinner customerSupplierSpinner = (Spinner) view.findViewById(R.id.customerSupplierSpinner);

        if (firstNameView!= null) firstNameView.setText(user.getFirstName());
        if (lastNameView!= null) lastNameView.setText(user.getLastName());
        if (fullNameView != null) fullNameView.setText(Utility.userNameToString(user));
        if (emailView!= null) emailView.setText(user.getEmail());
        if (addressView!= null) addressView.setText(user.getAddress());
        if (phoneView!= null){
            Editable phoneFormat = new SpannableStringBuilder(user.getPhoneNumber());
            PhoneNumberUtils.formatNanpNumber(phoneFormat);
            phoneView.setText(phoneFormat.toString());
        }
        if (birthdayView!= null) {} // TODO
        if (customerSupplierSpinner != null) customerSupplierSpinner.setSelection(user.getUserType() == UserType.CUSTOMER ? 0 : 1);
    }

    public static void result(View view, User user) {
        TextView firstNameView = (TextView) view.findViewById(R.id.userFirstName);
        TextView lastNameView = (TextView) view.findViewById(R.id.userLastName);
        TextView emailView = (TextView) view.findViewById(R.id.userEmail);
        TextView addressView = (TextView) view.findViewById(R.id.userAddress);
        TextView phoneView = (TextView) view.findViewById(R.id.userPhone);
        DatePicker birthdayView = null;//(DatePicker) view.findViewById(R.id.userBirthday);  // TODO
        ImageView imageView = (ImageView) view.findViewById(R.id.userThumbnail);
        Spinner customerSupplierSpinner = (Spinner) view.findViewById(R.id.customerSupplierSpinner);

        user.setCredentials(resultCredentials(view));
        if (firstNameView!= null) user.setFirstName(firstNameView.getText().toString());
        if (lastNameView!= null) user.setLastName(lastNameView.getText().toString());
        if (emailView!= null) user.setEmail(
                emailView.getText().toString());
        if (addressView!= null) user.setAddress(addressView.getText().toString());
        if (phoneView!= null) user.setPhoneNumber(phoneView.getText().toString());
        if (birthdayView!= null) {} // TODO
        if (imageView != null) {}// Do nothing
        if (customerSupplierSpinner != null) {
            user.setUserType(customerSupplierSpinner.getSelectedItemPosition() == 0 ? UserType.CUSTOMER : UserType.SUPPLIER);
        }
    }

    public static void apply(View view, BookQuery bookQuery) {
        TextView titleView = (TextView) view.findViewById(R.id.title_query);
        TextView authorView = (TextView) view.findViewById(R.id.author_query);
        Spinner genreSpinner = (Spinner) view.findViewById(R.id.genre_spinner);
        TextView fromPriceView = (TextView) view.findViewById(R.id.from_price);
        TextView toPriceView = (TextView) view.findViewById(R.id.to_price);

        if (titleView != null) titleView.setText(bookQuery.getTitleQuery());
        if (authorView != null) authorView.setText(bookQuery.getAuthorQuery());
        if (fromPriceView != null) fromPriceView.setText(bookQuery.getBeginPrice().setScale(0).toString());
        if (toPriceView != null) toPriceView.setText(bookQuery.getEndPrice().setScale(0).toString());
        if (genreSpinner != null) {
            if (genreSpinner instanceof MultiSpinner) {
                MultiSpinner multiSpinner = (MultiSpinner) genreSpinner;
                Book.Genre[] genres = Book.Genre.values();
                boolean[] selected = new boolean[genres.length];
                Set<Book.Genre> genresSet = bookQuery.getGenreSet();
                for (int i = 0; i < genres.length; i++) if (genresSet.contains(genres[i])) selected[i] = true;
                multiSpinner.setSelected(selected);
            } else {
                Book.Genre genreSelection = bookQuery.getGenreSet().isEmpty() ? Book.Genre.GENERAL : bookQuery.getGenreSet().iterator().next();
                genreSpinner.setSelection(genreSelection.ordinal());
            }
        }
    }

    public static void result(View view, BookQuery bookQuery) {
        TextView titleView = (TextView) view.findViewById(R.id.title_query);
        TextView authorView = (TextView) view.findViewById(R.id.author_query);
        // TODO and spinner
        Spinner genreSpinner = (Spinner) view.findViewById(R.id.genre_spinner);
        TextView fromPriceView = (TextView) view.findViewById(R.id.from_price);
        TextView toPriceView = (TextView) view.findViewById(R.id.to_price);

        if (titleView != null) bookQuery.setTitleQuery(titleView.getText().toString());
        if (authorView != null) bookQuery.setAuthorQuery(authorView.getText().toString());
        if (genreSpinner != null && !(genreSpinner instanceof MultiSpinner)) {
            bookQuery.getGenreSet().clear();
            bookQuery.getGenreSet().add((Book.Genre) genreSpinner.getSelectedItem());
        }
        if (fromPriceView != null && toPriceView != null) {
            try {
                bookQuery.setBeginPrice(new BigDecimal(fromPriceView.getText().toString()));
                bookQuery.setEndPrice(new BigDecimal(toPriceView.getText().toString()));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public static void apply(View view, Transaction transaction) {
        // TODO
        TextView transactionAddressView = (TextView) view.findViewById(R.id.transactionAddress);
        TextView transactionDateText = (TextView) view.findViewById(R.id.transactionDate);

        if (transactionAddressView != null) transactionAddressView.setText(transaction.getShippingAddress());
        if (transactionDateText != null) transactionDateText.setText(Utility.datetimeToString(transaction.getDate()));

    }


    public static void setDateRange(View view, Date fromDate, Date toDate) {
        TextView fromView = (TextView) view.findViewById(R.id.fromDate);
        TextView toView = (TextView) view.findViewById(R.id.toDate);

        if (fromView != null) fromView.setText(Utility.dateToString(fromDate));
        if (toView != null) toView.setText(Utility.dateToString(toDate));
    }

    public static void apply(View view, ImageEntity imageEntity) {
        if (imageEntity == null) imageEntity = new ImageEntity();
        ImageView bookImageView = (ImageView) view.findViewById(R.id.bookImage);
        ImageView userImageView = (ImageView) view.findViewById(R.id.userThumbnail);
        if (bookImageView != null) Utility.setImageByBytes(bookImageView,imageEntity.getBytes(),R.drawable.image_book);
        if (userImageView != null) Utility.setImageByBytes(userImageView,imageEntity.getBytes(),R.drawable.image_user);
    }


    ////////

    public interface Applier<T> {
        void apply(T object);
        void result(T t);
    }

    public static abstract class AbstractApplier<T> implements Applier<T> {
        AbstractApplier() {}
        public void result(T t) { throw new UnsupportedOperationException(); }
    }

    public static Applier<Book> book(View view) {
        return new AbstractApplier<Book>() {
            TextView titleView = (TextView) view.findViewById(R.id.bookTitle);
            TextView authorView = (TextView) view.findViewById(R.id.bookAuthor);
            ImageView imageView = (ImageView) view.findViewById(R.id.bookImage);
            public void apply(Book book) {
                if (titleView != null) titleView.setText(book.getTitle());
                if (authorView != null) authorView.setText(book.getAuthor());
                if (imageView != null)
                    Utility.setImageById(imageView, book.getImageId(), R.drawable.image_book);
            }
            public void result(Book book) {
                if (titleView != null) book.setTitle(titleView.getText().toString());
                if (authorView != null) book.setAuthor(authorView.getText().toString());
            }
        };
    }

}
