package com.hgyw.bookshare.app_activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.ProgressDialogAsyncTask;
import com.hgyw.bookshare.app_drivers.SimpleTextWatcher;
import com.hgyw.bookshare.app_drivers.Utility;
import com.hgyw.bookshare.app_fragments.CartFragment;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.entities.IdReference;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.exceptions.OrdersTransactionException;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.Cart;
import com.hgyw.bookshare.logicAccess.CustomerAccess;


/**
 * A class represents new transaction activity.
 */
public class NewTransactionActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    private Cart cart;
    private CustomerAccess cAccess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cAccess = AccessManagerFactory.getInstance().getCustomerAccess();
        cart = cAccess.getCart();
        setContentView(R.layout.activity_transaction);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set title of activity
        setTitle(R.string.new_transaction_title);

        //Instantiate new cart fragment to show at the bottom of the screen
        Fragment cartFragment = CartFragment.newInstance(false);
        getFragmentManager().beginTransaction()
                .replace(R.id.cart_container, cartFragment)
                .commit();

        //set total sum
        String totalSum = Utility.moneyToNumberString(cart.calculateTotalSum());
        ((TextView)findViewById(R.id.total_sum)).setText(totalSum);
        //set default address as customer address
        EditText shippingAddress = ((EditText)(findViewById(R.id.shipping_address)));
        retrieveCustomerAccess(shippingAddress);

        //set listeners to address and credit number
        shippingAddress.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        cart.getTransaction().setShippingAddress(s.toString());
                    }
                });

        ((EditText)(findViewById(R.id.credit_number)))
                .addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        cart.getTransaction().setCreditCard(s.toString());
                    }
                });
    }

    private void retrieveCustomerAccess(EditText shippingAddress){
            new ProgressDialogAsyncTask<Void, Void, String>(this, R.string.loading_information) {
                @Override
                protected String retrieveDataAsync(Void... params) {
                    String s = cAccess.retrieveUserDetails().getAddress();
                    System.out.println("Customer Address: " + s);
                    return s;
                }

                @Override
                protected void doByData(String s) {
                    shippingAddress.setText(s);
                }
            }.execute();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //on back pressed go back to cart fragment
        IntentsFactory.newCartIntent(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transaction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_confirm:
                return confirmTransaction();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean confirmTransaction() {
        Transaction transaction = cart.getTransaction();
        //fictive rules for credit number and address
        boolean wrongCreditCard = transaction.getCreditCard().trim().length() < 8;
        boolean wrongAddress = transaction.getShippingAddress().trim().isEmpty();
        if (wrongCreditCard || wrongAddress){
            new AlertDialog.Builder(this)
                    .setMessage(wrongAddress ? R.string.wrong_address_message : R.string.wrong_credit_message)
                    .setNeutralButton(R.string.ok,(d,w)->{})
                    .create().show();
            return true;
        }

        //ensure the confirmation
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setMessage(R.string.confirm_order_message)
                .setPositiveButton(R.string.yes, this)
                .setNeutralButton(R.string.no, this)
                .create().show();
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                /**
                 * This async-task try to perform new transaction.
                 * If OrdersTransactionException is thrown it returns it to the main thread
                 */
                new ProgressDialogAsyncTask<Void, Void, OrdersTransactionException>(this) {
                    public IdReference transaction;

                    @Override
                    protected OrdersTransactionException retrieveDataAsync(Void... params) {
                        try { transaction = cAccess.performNewTransaction(); return null; }
                        catch (OrdersTransactionException e) { return e; }
                    }

                    @Override
                    protected void doByData(OrdersTransactionException e) {
                        if (e == null) { //means all is OK
                            Toast.makeText(context, R.string.toast_transaction_ok, Toast.LENGTH_SHORT).show();
                            Intent transactionIntent = IntentsFactory.newEntityIntent(context, transaction);
                            finish();
                            startActivity(transactionIntent);
                        } else {
                            //if something got wrong return to the activity
                            new AlertDialog.Builder(context)
                                    .setMessage(R.string.transaction_error_message)
                                    .setNeutralButton(R.string.ok, (d,w)->{})
                                    .create().show();
                        }
                    }
                }.execute();
                break;
            default: break;
        }
    }
}
