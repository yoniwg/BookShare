package com.hgyw.bookshare.app_drivers;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.entities.BookReview;
import com.hgyw.bookshare.entities.BookSupplier;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.entities.UserType;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.GeneralAccess;

import java.math.BigDecimal;
import java.util.List;

/**
 * Update views by full data of items.
 */
public class ObjectToViewUpdates {

    public static void updateBookReviewView(View view, BookReview bookReview, User customer, boolean withImage) {
        ObjectToViewAppliers.apply(view, bookReview);
        ObjectToViewAppliers.apply(view, customer, withImage);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.reviewDescription);
        if (descriptionTextView != null) {
            String description = bookReview.getDescription();
            if (description.isEmpty()) descriptionTextView.setVisibility(View.GONE);
        }
    }

    public static void updateBookSupplierBuyView(View view, BookSupplier bookSupplier, User supplier, boolean withImage) {
        ObjectToViewAppliers.apply(view, bookSupplier);
        ObjectToViewAppliers.apply(view, supplier, withImage);
    }

    public static void updateTransactionListItem(View view, Transaction transaction, BigDecimal totalPrice, List<User> suppliersList) {
        ObjectToViewAppliers.apply(view, transaction);
        TextView transactionTotalPrice = (TextView) view.findViewById(R.id.total_price);
        TextView transactionSuppliers = (TextView) view.findViewById(R.id.supplier_names_list);

        if (transactionTotalPrice != null){
            transactionTotalPrice.setText(Utility.moneyToNumberString(totalPrice));
        }
        if (transactionSuppliers != null){
            String transactionSuppliersText;
            transactionSuppliersText = Stream.of(suppliersList).map(User::getLastName).collect(Collectors.joining(", "));
            transactionSuppliers.setText(transactionSuppliersText);
        }
    }


}
