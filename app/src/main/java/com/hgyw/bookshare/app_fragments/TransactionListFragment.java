package com.hgyw.bookshare.app_fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.hgyw.bookshare.R;
import com.hgyw.bookshare.app_drivers.DateRangeBar;
import com.hgyw.bookshare.app_drivers.GoodAsyncListAdapter;
import com.hgyw.bookshare.app_drivers.IntentsFactory;
import com.hgyw.bookshare.app_drivers.ObjectToViewUpdates;
import com.hgyw.bookshare.entities.Transaction;
import com.hgyw.bookshare.entities.User;
import com.hgyw.bookshare.logicAccess.AccessManagerFactory;
import com.hgyw.bookshare.logicAccess.CustomerAccess;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by haim7 on 23/05/2016.
 */
public class TransactionListFragment extends ListFragment implements TitleFragment {

    private Activity activity;
    GoodAsyncListAdapter<Transaction> adapter;

    @Override public void onAttach(Context context) {super.onAttach(context);activity = (Activity) context;}
    @Override public void onAttach(Activity activity) {super.onAttach(activity);this.activity = activity;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content_with_date_range, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DateRangeBar dateRangeBar = (DateRangeBar) view.findViewById(R.id.dateRangeBar);
        dateRangeBar.setDateRangeListener(this::updateListAdapter);
        updateListAdapter(dateRangeBar);
        setEmptyText(getString(R.string.no_items_list_view));
    }

    private void updateListAdapter(DateRangeBar dateRangeBar) {
        CustomerAccess cAccess = AccessManagerFactory.getInstance().getCustomerAccess();

        Date dateFrom = dateRangeBar.getDateFrom();
        Date dateTo = dateRangeBar.getDateTo();

        adapter = new GoodAsyncListAdapter<Transaction>(activity, R.layout.transaction_list_item, this) {
            @Override
            public List<Transaction> retrieveList() {
                return cAccess.retrieveTransactions(dateFrom, dateTo);
            }

            @Override
            public Object[] retrieveData(Transaction transaction) {
                return new Object[] {
                    cAccess.calcTotalPriceOfTransaction(transaction),
                    cAccess.getSuppliersOfTransaction(transaction)
                };
            }

            @Override
            public void applyDataOnView(Transaction transaction, Object[] data, View view) {
                List<User> transactionSuppliers = Stream.of((List<User>) data[1]).distinct().collect(Collectors.toList());
                ObjectToViewUpdates.updateTransactionListItem(view, transaction, (BigDecimal) data[0], transactionSuppliers);
            }

        };
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Transaction transaction = adapter.getItem(position);
        Intent intent = IntentsFactory.newEntityIntent(activity, transaction);
        startActivity(intent);
    }

    @Override
    public int getFragmentTitle() {
        return R.string.transactions;
    }

    @Override
    public void onDestroy() {
        if (adapter != null) adapter.cancel();
        super.onDestroy();
    }

}
