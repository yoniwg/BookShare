package com.hgyw.bookshare.app_drivers;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.Book;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.Order;
import com.hgyw.bookshare.entities.ImageEntity;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.math.BigDecimal;
import java.util.List;

/**
 * Update views by full data of items.
 */
public class ObjectToViewUpdates {

    public static void updateBookReviewView(View view, BookReview bookReview, User customer, ImageEntity userImage) {
        ObjectToViewAppliers.apply(view, bookReview);
        ObjectToViewAppliers.apply(view, customer);
        ObjectToViewAppliers.apply(view, userImage);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.reviewDescription);
        if (descriptionTextView != null) {
            String description = bookReview.getDescription();
            if (description.isEmpty()) descriptionTextView.setVisibility(View.GONE);
        }
    }

    public static void updateBookSupplierBuyView(View view, BookSupplier bookSupplier, User supplier, ImageEntity userImage) {
        ObjectToViewAppliers.apply(view, bookSupplier);
        ObjectToViewAppliers.apply(view, supplier);
        ObjectToViewAppliers.apply(view, userImage);
    }

    public static void updateTransactionListItem(View view, Transaction transaction, BigDecimal totalPrice, List<User> suppliersList) {
        updateTransaction(view, transaction, totalPrice);
        TextView transactionSuppliers = (TextView) view.findViewById(R.id.supplier_names_list);

        if (transactionSuppliers != null){
            String transactionSuppliersText;
            transactionSuppliersText = Stream.of(suppliersList).map(User::getLastName).collect(Collectors.joining(", "));
            transactionSuppliers.setText(transactionSuppliersText);
        }
    }

    public static void updateTransaction(View view, Transaction transaction, BigDecimal totalPrice) {
        ObjectToViewAppliers.apply(view, transaction);
        TextView transactionTotalPrice = (TextView) view.findViewById(R.id.transactionTotalPrice);

        if (transactionTotalPrice != null){
            transactionTotalPrice.setText(Utility.moneyToNumberString(totalPrice));
        }
    }


    public static void setListenerToOrder(View view, Order order) {
        GeneralAccess access = AccessManagerFactory.getInstance().getGeneralAccess();
        Context context = view.getContext();

        Utility.setListenerForAll(view, v -> {
            new CancelableLoadingDialogAsyncTask<String>(context) {
                @Override
                protected String retrieveDataAsync() {
                    Book book = access.retrieve(Book.class, OrderUtility.getBookId(order));
                    return book.getAuthor();
                }

                @Override
                protected void doByData(String query) {
                    Utility.startSearchActivity(context, query);
                }

                @Override
                protected void onCancel() {}
            }.execute();
        }, R.id.bookAuthor, R.id.bookAuthorIcon);

        Utility.setListenerForAll(view, v -> {
            IdReference supplier = IdReference.of(User.class, OrderUtility.getSupplierId(order));
            context.startActivity(IntentsFactory.newEntityIntent(context, supplier));
        }, R.id.userFirstName, R.id.userLastName, R.id.userFullName, R.id.userFullNameIcon);

    }

    public static void setListenerToUser(View view, User user) {
        Context context = view.getContext();

        Utility.setListenerForAll(view, v -> {
            String phoneNumber = user.getPhoneNumber();

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + Uri.encode(phoneNumber)));
            try {context.startActivity(intent);} catch (ActivityNotFoundException ignored) {}
        }, R.id.userPhone);

        Utility.setListenerForAll(view, v -> {
            String email = user.getEmail();
            String[] addresses = new String[]{email};
            String subject = context.getString(R.string.mail_from_app) + " " + context.getString(R.string.app_name);

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + Uri.encode(email)));
            intent.putExtra(Intent.EXTRA_EMAIL, addresses);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            try {context.startActivity(intent);} catch (ActivityNotFoundException ignored) {}
        }, R.id.userEmail, R.id.userEmailIcon);


        Utility.setListenerForAll(view, v -> {
            String address = user.getAddress();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("geo:0,0?q=" + Uri.encode(address)));
            try {context.startActivity(intent);} catch (ActivityNotFoundException ignored) {}
        }, R.id.userAddress);

    }
    public static void setListenerToTransaction(View view, Transaction transaction) {
        Context context = view.getContext();

        Utility.setListenerForAll(view, v -> {
            String address = transaction.getShippingAddress();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("geo:0,0?q=" + Uri.encode(address)));
            try {context.startActivity(intent);} catch (ActivityNotFoundException ignored) {}
        }, R.id.transactionAddress, R.id.transactionAddressIcon);

    }
}
