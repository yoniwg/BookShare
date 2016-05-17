package com.hgyw.bookshare.app_activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hgyw.bookshare.R;
import com.hgyw.bookshare.SimpleTextWatcher;
import com.hgyw.bookshare.Utility;
import com.hgyw.bookshare.app_fragments.CartFragment;
import com.hgyw.bookshare.app_fragments.IntentsFactory;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.exceptions.OrdersTransactionException;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;


/**
 *
 */
public class TransactionActivity extends AppCompatActivity {


    private CustomerAccess cAccess;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cAccess = AccessManagerFactory.getInstance().getCustomerAccess();
        setContentView(R.layout.activity_transaction);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.transaction_title);
        Fragment cartFragment = CartFragment.newInstance(false);
        getFragmentManager().beginTransaction()
                .replace(R.id.cart_container, cartFragment)
                .commit();
        //set total sum
        String totalSum = Utility.moneyToNumberString(cAccess.getCart().calculateTotalSum());
        ((TextView)findViewById(R.id.total_sum)).setText(totalSum);

        //set listeners to address and credit number
        ((EditText)(findViewById(R.id.shipping_address)))
                .addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        cAccess.getCart().getTransaction().setShippingAddress(s.toString());
                    }
                });

        ((EditText)(findViewById(R.id.credit_number)))
                .addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        cAccess.getCart().getTransaction().setCreditCard(s.toString());
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        Transaction transaction = cAccess.getCart().getTransaction();
        boolean wrongCreditCard = transaction.getCreditCard().trim().length() < 8;
        boolean wrongAddress = transaction.getShippingAddress().trim().isEmpty();
        if (wrongCreditCard || wrongAddress){
            new AlertDialog.Builder(this)
                    .setMessage(wrongAddress ? R.string.wrong_address_message : R.string.wrong_credit_message)
                    .setNeutralButton(R.string.ok,(d,w)->{}).create().show();
            return true;
        }
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setMessage(R.string.confirm_order_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    try {
                        cAccess.performNewTransaction();
                        Toast.makeText(this,R.string.toast_transaction_ok, Toast.LENGTH_SHORT).show();
                        Intent transactionIntent = IntentsFactory.newBookListIntent(this,null);
                        startActivity(transactionIntent);
                    } catch (OrdersTransactionException e) {
                        new AlertDialog.Builder(this)
                                .setMessage(R.string.transaction_error_message)
                                .setNeutralButton(R.string.ok,(d,w)->{}).create().show();
                    }
                })
                .setNeutralButton(R.string.no, (dialog, which) -> {
                });
        builder2.create().show();
        return true;
    }
}
